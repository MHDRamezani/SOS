package org.n52.sos.proxy.da;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.n52.io.request.IoParameters;
import org.n52.series.db.assembler.core.CategoryAssembler;
import org.n52.series.db.assembler.core.DatasetAssembler;
import org.n52.series.db.assembler.core.FeatureAssembler;
import org.n52.series.db.assembler.core.OfferingAssembler;
import org.n52.series.db.assembler.core.PhenomenonAssembler;
import org.n52.series.db.assembler.core.PlatformAssembler;
import org.n52.series.db.assembler.core.ProcedureAssembler;
import org.n52.series.db.assembler.core.ServiceAssembler;
import org.n52.series.db.assembler.core.TagAssembler;
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.CategoryEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.OfferingEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.QuantityDataEntity;
import org.n52.series.db.beans.ServiceEntity;
import org.n52.series.db.beans.TagEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.series.db.old.dao.DbQueryFactory;
import org.n52.series.db.query.DatasetQuerySpecifications;
import org.n52.series.db.repositories.core.DataRepository;
import org.n52.series.db.repositories.core.DatasetRepository;
import org.n52.series.db.repositories.core.UnitRepository;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class InsertionRepository {

    private static final Logger LOGGER = getLogger(InsertionRepository.class);

    @Inject
    private DbQueryFactory dbQueryFactory;

    @Inject
    private CategoryAssembler categoryAssembler;

    @Inject
    private FeatureAssembler featureAssembler;

    @Inject
    private OfferingAssembler offeringAssembler;

    @Inject
    private PhenomenonAssembler phenomenonAssembler;

    @Inject
    private ProcedureAssembler procedureAssembler;

    @Inject
    private PlatformAssembler platformAssembler;

    @Inject
    private TagAssembler tagAssembler;

    @Inject
    private ServiceAssembler serviceAssembler;

    @Inject
    private DatasetAssembler<?> datasetAssembler;

    @Inject
    private DatasetRepository datasetRepository;

    @Inject
    private UnitRepository unitRepository;

    @Inject
    private DataRepository dataRepository;

    public ServiceEntity insertService(ServiceEntity service) {
        return serviceAssembler.getOrInsertInstance(service);
    }

    public synchronized DatasetEntity insertDataset(DatasetEntity dataset) {
        ProcedureEntity procedure = insertProcedure(dataset.getProcedure());
        CategoryEntity category = insertCategory(dataset.getCategory());
        OfferingEntity offering = insertOffering(dataset.getOffering());
        AbstractFeatureEntity<?> feature = insertFeature(dataset.getFeature());
        PhenomenonEntity phenomenon = insertPhenomenon(dataset.getPhenomenon());
        PlatformEntity platform = insertPlatform(dataset.getPlatform());
        UnitEntity unit = insertUnit(dataset.getUnit());
        Set<TagEntity> insertTags = new LinkedHashSet<>();
        if (dataset.hasTagss()) {
            insertTags = insertTags(dataset.getTags());
        }
        return insertDataset(dataset, category, procedure, offering, feature, phenomenon, platform, unit, insertTags);
    }

    private DatasetEntity insertDataset(DatasetEntity dataset, CategoryEntity category, ProcedureEntity procedure,
            OfferingEntity offering, AbstractFeatureEntity<?> feature, PhenomenonEntity phenomenon,
            PlatformEntity platform, UnitEntity unit, Set<TagEntity> tags) {
        dataset.setCategory(category);
        dataset.setProcedure(procedure);
        dataset.setOffering(offering);
        dataset.setFeature(feature);
        dataset.setPhenomenon(phenomenon);
        dataset.setPlatform(platform);
        dataset.setUnit(unit);
        if (tags != null && !tags.isEmpty()) {
            dataset.setTags(tags);
        }
        return datasetAssembler.getOrInsertInstance(dataset);
    }

    private OfferingEntity insertOffering(OfferingEntity offering) {
        return offeringAssembler.getOrInsertInstance(offering);
    }

    private ProcedureEntity insertProcedure(ProcedureEntity procedure) {
        return procedureAssembler.getOrInsertInstance(procedure);
    }

    private CategoryEntity insertCategory(CategoryEntity category) {
        return categoryAssembler.getOrInsertInstance(category);
    }

    private AbstractFeatureEntity<?> insertFeature(AbstractFeatureEntity feature) {
        return featureAssembler.getOrInsertInstance(feature);
    }

    private PhenomenonEntity insertPhenomenon(PhenomenonEntity phenomenon) {
        return phenomenonAssembler.getOrInsertInstance(phenomenon);
    }

    private PlatformEntity insertPlatform(PlatformEntity platform) {
        return platformAssembler.getOrInsertInstance(platform);
    }

    private Set<TagEntity> insertTags(Collection<TagEntity> tags) {
        return tags.stream().filter(Objects::nonNull).map(t -> insertTag(t)).collect(Collectors.toSet());
    }

    private TagEntity insertTag(TagEntity tag) {
        return tagAssembler.getOrInsertInstance(tag);
    }

    private UnitEntity insertUnit(UnitEntity unit) {
        if (unit != null && unit.isSetIdentifier()) {
            UnitEntity instance = unitRepository.getInstance(unit);
            if (instance != null) {
                return instance;
            }
            return unitRepository.saveAndFlush(unit);
        }
        return null;
    }

    public synchronized <T extends DataEntity<?>> T insertData(DatasetEntity dataset, T  data) {
        data.setDataset(dataset);
        boolean minChanged = false;
        boolean maxChanged = false;
        if (!dataset.isSetFirstValueAt() || (dataset.isSetFirstValueAt()
                && dataset.getFirstValueAt().after(data.getSamplingTimeStart()))) {
            minChanged = true;
            dataset.setFirstValueAt(data.getSamplingTimeStart());
        }
        if (!dataset.isSetLastValueAt() || (dataset.isSetLastValueAt()
                && dataset.getLastValueAt().before(data.getSamplingTimeEnd()))) {
            maxChanged = true;
            dataset.setLastValueAt(data.getSamplingTimeEnd());
        }
        DataEntity<?> insertedData = null;
        if (minChanged) {
            if (dataset.getFirstObservation() != null) {
                data.setId(dataset.getFirstObservation().getId());
            }
            insertedData = (DataEntity<?>) dataRepository.saveAndFlush(data);
            dataset.setFirstObservation(insertedData);
        }
        if (maxChanged) {
            if (insertedData != null) {
                dataset.setLastObservation(insertedData);
            } else {
                if (dataset.getLastObservation() != null) {
                    data.setId(dataset.getLastObservation().getId());
                }
                dataset.setLastObservation(((DataEntity<?>) dataRepository.saveAndFlush(data)));
            }
        }

        if (insertedData instanceof QuantityDataEntity) {
            if (minChanged) {
                dataset.setFirstQuantityValue(((QuantityDataEntity) insertedData).getValue());
            }
            if (maxChanged) {
                dataset.setLastQuantityValue(((QuantityDataEntity) insertedData).getValue());
            }
        }
        if (minChanged && maxChanged) {
            datasetRepository.saveAndFlush(dataset);
        }
        return (T) insertedData;
    }

    public synchronized DatasetEntity updateData(DatasetEntity dataset, DataEntity<?> data) {
        dataRepository.saveAndFlush(data);
        return updateDataset(dataset);
    }

    public synchronized DatasetEntity updateDataset(DatasetEntity dataset) {
        return datasetRepository.saveAndFlush(dataset);
    }

    public DatasetQuerySpecifications getDatasetQuerySpecification() {
        return getDatasetQuerySpecification(IoParameters.createDefaults());
    }

    public DatasetQuerySpecifications getDatasetQuerySpecification(IoParameters parameters) {
        return DatasetQuerySpecifications.of(dbQueryFactory.createFrom(IoParameters.createDefaults()),
                datasetAssembler.getEntityManager());
    }

}