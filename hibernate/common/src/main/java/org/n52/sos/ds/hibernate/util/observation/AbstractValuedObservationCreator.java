package org.n52.sos.ds.hibernate.util.observation;

import java.util.LinkedList;
import java.util.List;

import org.apache.xmlbeans.XmlObject;
import org.n52.series.db.beans.DataArrayDataEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.shetland.ogc.ows.exception.CodedException;
import org.n52.shetland.ogc.ows.exception.NoApplicableCodeException;
import org.n52.shetland.ogc.swe.SweAbstractDataComponent;
import org.n52.shetland.ogc.swe.SweAbstractDataRecord;
import org.n52.shetland.ogc.swe.SweDataArray;
import org.n52.shetland.ogc.swe.encoding.SweAbstractEncoding;
import org.n52.shetland.ogc.swe.encoding.SweTextEncoding;
import org.n52.shetland.util.JavaHelper;
import org.n52.svalbard.decode.DecoderRepository;
import org.n52.svalbard.decode.exception.DecodingException;
import org.n52.svalbard.util.CodingHelper;
import org.n52.svalbard.util.XmlHelper;

public abstract class AbstractValuedObservationCreator<T> implements ValuedObservationVisitor<T> {

    private DecoderRepository decoderRepository;
    private boolean noValues;

    public AbstractValuedObservationCreator(DecoderRepository decoderRepository) {
        this(decoderRepository, false);
    }

    public AbstractValuedObservationCreator(DecoderRepository decoderRepository, boolean noValues) {
        this.decoderRepository = decoderRepository;
        this.noValues = noValues;
    }

    protected DecoderRepository getDecoderRepository() {
        return decoderRepository;
    }

    protected boolean isNoValues() {
        return noValues;
    }

    protected Object decode(XmlObject xml) throws DecodingException {
        return getDecoderRepository().getDecoder(CodingHelper.getDecoderKey(xml)).decode(xml);
    }

    protected SweDataArray createSweDataArray(DataArrayDataEntity o) throws CodedException {
        try {
            SweDataArray array = new SweDataArray();
            decode(XmlHelper.parseXmlString(o.getResultTemplate().getEncoding()));
            array.setEncoding(
                    (SweAbstractEncoding) decode(XmlHelper.parseXmlString(o.getResultTemplate().getEncoding())));
            array.setElementType(
                    (SweAbstractDataComponent) decode(XmlHelper.parseXmlString(o.getResultTemplate().getStructure())));
            if (isNoValues()) {
                if (o.isSetStringValue()) {
                    array.setXml(null);
                    List<List<String>> values = new LinkedList<>();
                    for (String block : o.getStringValue()
                            .split(((SweTextEncoding) array.getEncoding()).getBlockSeparator())) {
                        List<String> v = new LinkedList<>();
                        for (String value : block.split(((SweTextEncoding) array.getEncoding()).getTokenSeparator())) {
                            v.add(value);
                        }
                        values.add(v);
                    }
                    array.setValues(values);
                } else if (o.getValue() != null && !o.getValue()
                        .isEmpty()) {
                    int i = ((SweAbstractDataRecord) array.getElementType()).getFieldIndexByIdentifier(o.getDataset()
                            .getPhenomenon()
                            .getIdentifier()) == 0 ? 1 : 0;
                    List<List<String>> values = new LinkedList<>();
                    for (DataEntity<?> v : o.getValue()) {
                        List<String> value = new LinkedList<>();
                        if (i == 0) {
                            value.add(v.getDataset()
                                    .getPhenomenon()
                                    .getName());
                            value.add(JavaHelper.asString(v.getValue()));
                        } else {
                            value.add(JavaHelper.asString(v.getValue()));
                            value.add(v.getDataset()
                                    .getPhenomenon()
                                    .getName());
                        }
                        values.add(value);
                    }
                    array.setValues(values);
                }
            }
            return array;
        } catch (DecodingException e) {
            throw new NoApplicableCodeException().causedBy(e)
                    .withMessage("Error while creating SweDataArray from database entity!");
        }
    }

}
