/*
 * Copyright (C) 2012-2018 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.sos.ds.hibernate.util.observation;

import org.n52.series.db.beans.data.Data.BlobData;
import org.n52.series.db.beans.data.Data.BooleanData;
import org.n52.series.db.beans.data.Data.CategoryData;
import org.n52.series.db.beans.data.Data.ComplexData;
import org.n52.series.db.beans.data.Data.CountData;
import org.n52.series.db.beans.data.Data.DataArrayData;
import org.n52.series.db.beans.data.Data.GeometryData;
import org.n52.series.db.beans.data.Data.ProfileData;
import org.n52.series.db.beans.data.Data.QuantityData;
import org.n52.series.db.beans.data.Data.ReferencedData;
import org.n52.series.db.beans.data.Data.TextData;
import org.n52.series.db.beans.data.Data;
import org.n52.shetland.ogc.ows.exception.OwsExceptionReport;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public interface ObservationVisitor<T> {

    @SuppressWarnings("rawtypes")
    T visit(Data o) throws OwsExceptionReport;

    T visit(QuantityData o) throws OwsExceptionReport;

    T visit(BlobData o) throws OwsExceptionReport;

    T visit(BooleanData o) throws OwsExceptionReport;

    T visit(CategoryData o) throws OwsExceptionReport;

    T visit(ComplexData o) throws OwsExceptionReport;

    T visit(CountData o) throws OwsExceptionReport;

    T visit(GeometryData o) throws OwsExceptionReport;

    T visit(TextData o) throws OwsExceptionReport;

    T visit(DataArrayData o) throws OwsExceptionReport;

    T visit(ProfileData o)
            throws OwsExceptionReport;

    T visit(ReferencedData o)
            throws OwsExceptionReport;

}
