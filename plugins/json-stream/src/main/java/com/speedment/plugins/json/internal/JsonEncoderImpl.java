/**
 *
 * Copyright (c) 2006-2016, Speedment, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); You may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.speedment.plugins.json.internal;

import com.speedment.plugins.json.JsonCollector;
import com.speedment.plugins.json.JsonEncoder;
import com.speedment.common.dbmodel.Project;
import com.speedment.runtime.field.BooleanField;
import com.speedment.runtime.field.ByteField;
import com.speedment.runtime.field.CharField;
import com.speedment.runtime.field.DoubleField;
import com.speedment.runtime.field.Field;
import com.speedment.runtime.field.FloatField;
import com.speedment.runtime.field.IntField;
import com.speedment.runtime.field.LongField;
import com.speedment.runtime.field.ReferenceField;
import com.speedment.runtime.field.ShortField;
import com.speedment.runtime.field.method.BooleanGetter;
import com.speedment.runtime.field.method.ByteGetter;
import com.speedment.runtime.field.method.CharGetter;
import com.speedment.runtime.field.method.DoubleGetter;
import com.speedment.runtime.field.method.Finder;
import com.speedment.runtime.field.method.FloatGetter;
import com.speedment.runtime.field.method.Getter;
import com.speedment.runtime.field.method.IntGetter;
import com.speedment.runtime.field.method.LongGetter;
import com.speedment.runtime.field.method.ReferenceGetter;
import com.speedment.runtime.field.method.ShortGetter;
import com.speedment.runtime.field.trait.HasFinder;
import com.speedment.runtime.manager.Manager;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.speedment.plugins.json.internal.JsonUtil.jsonField;
import static com.speedment.common.invariant.NullUtil.requireNonNulls;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static com.speedment.common.invariant.NullUtil.requireNonNulls;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

/**
 * The default implementation of the {@link JsonEncoder} interface.
 * 
 * @param <ENTITY>  the entity type
 * 
 * @author  Emil Forslund
 * @since   1.0.0
 */
final class JsonEncoderImpl<ENTITY> implements JsonEncoder<ENTITY> {
    
    private final Map<String, Function<ENTITY, String>> getters;
    private final Project project;
    private final Manager<ENTITY> manager;

    /**
     * Constructs an empty JsonEncoder with no fields added to the output
     * renderer.
     */
    JsonEncoderImpl(Project project, Manager<ENTITY> manager) {
        this.getters = new LinkedHashMap<>();
        this.project = requireNonNull(project);
        this.manager = requireNonNull(manager);
    }

    /**************************************************************************/
    /*                             Getters                                    */
    /**************************************************************************/
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Manager<ENTITY> getManager() {
        return manager;
    }

    /**************************************************************************/
    /*                          Field Putters                                 */
    /**************************************************************************/
    
    /**
     * {@inheritDoc}
     */
    @Override
    public <D, V> JsonEncoder<ENTITY> put(ReferenceField<ENTITY, D, V> field) {
        return putHelper(field, ReferenceField::getter, this::put);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public <D> JsonEncoder<ENTITY> putByte(ByteField<ENTITY, D> field) {
        return putHelper(field, ByteField::getter, this::putByte);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public <D> JsonEncoder<ENTITY> putShort(ShortField<ENTITY, D> field) {
        return putHelper(field, ShortField::getter, this::putShort);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public <D> JsonEncoder<ENTITY> putInt(IntField<ENTITY, D> field) {
        return putHelper(field, IntField::getter, this::putInt);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public <D> JsonEncoder<ENTITY> putLong(LongField<ENTITY, D> field) {
        return putHelper(field, LongField::getter, this::putLong);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public <D> JsonEncoder<ENTITY> putFloat(FloatField<ENTITY, D> field) {
        return putHelper(field, FloatField::getter, this::putFloat);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public <D> JsonEncoder<ENTITY> putDouble(DoubleField<ENTITY, D> field) {
        return putHelper(field, DoubleField::getter, this::putDouble);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public <D> JsonEncoder<ENTITY> putChar(CharField<ENTITY, D> field) {
        return putHelper(field, CharField::getter, this::putChar);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public <D> JsonEncoder<ENTITY> putBoolean(BooleanField<ENTITY, D> field) {
        return putHelper(field, BooleanField::getter, this::putBoolean);
    }
    
    private <F extends Field<ENTITY>, G extends Getter<ENTITY>> JsonEncoder<ENTITY> putHelper(
        F field, Function<F, G> getter, BiFunction<String, G, JsonEncoder<ENTITY>> putter) {
        
        requireNonNulls(field, getter, putter);
        final String columnName = jsonField(project, field.identifier());
        return putter.apply(columnName, getter.apply(field));
    }

    /**************************************************************************/
    /*                        Put Labels with Getters                         */
    /**************************************************************************/
    
    /**
     * {@inheritDoc}
     */
    @Override
    public <T> JsonEncoder<ENTITY> put(String label, ReferenceGetter<ENTITY, T> getter) {
        return putHelper(label, e -> jsonValue(getter.apply(e)));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public JsonEncoder<ENTITY> putByte(String label, ByteGetter<ENTITY> getter) {
        return putHelper(label, e -> jsonValue(getter.applyAsByte(e)));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public JsonEncoder<ENTITY> putShort(String label, ShortGetter<ENTITY> getter) {
        return putHelper(label, e -> jsonValue(getter.applyAsShort(e)));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public JsonEncoder<ENTITY> putInt(String label, IntGetter<ENTITY> getter) {
        return putHelper(label, e -> jsonValue(getter.applyAsInt(e)));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public JsonEncoder<ENTITY> putLong(String label, LongGetter<ENTITY> getter) {
        return putHelper(label, e -> jsonValue(getter.applyAsLong(e)));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public JsonEncoder<ENTITY> putFloat(String label, FloatGetter<ENTITY> getter) {
        return putHelper(label, e -> jsonValue(getter.applyAsFloat(e)));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public JsonEncoder<ENTITY> putDouble(String label, DoubleGetter<ENTITY> getter) {
        return putHelper(label, e -> jsonValue(getter.applyAsDouble(e)));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public JsonEncoder<ENTITY> putChar(String label, CharGetter<ENTITY> getter) {
        return putHelper(label, e -> jsonValue(getter.applyAsChar(e)));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public JsonEncoder<ENTITY> putBoolean(String label, BooleanGetter<ENTITY> getter) {
        return putHelper(label, e -> jsonValue(getter.applyAsBoolean(e)));
    }
    
    private JsonEncoder<ENTITY> putHelper(String label, Function<ENTITY, String> jsonValue) {
        requireNonNull(label);
        getters.put(label, e -> "\"" + label + "\":" + jsonValue.apply(e));
        return this;
    }
    
    /**************************************************************************/
    /*                        Put Fields with Finders                         */
    /**************************************************************************/
    
    /**
     * {@inheritDoc}
     */
    @Override
    public <FK_ENTITY, FIELD extends Field<ENTITY> & HasFinder<ENTITY, FK_ENTITY>> 
    JsonEncoder<ENTITY> put(FIELD field, JsonEncoder<FK_ENTITY> encoder) {
        requireNonNulls(field, encoder);
        final String columnName = jsonField(project, field.identifier());
        final Finder<ENTITY, FK_ENTITY> entityFinder = (e, m) -> field.finder(m).apply(e);
        return put(columnName, entityFinder, encoder);
    }

    /**************************************************************************/
    /*                        Put Labels with Finders                         */
    /**************************************************************************/
    
    /**
     * {@inheritDoc}
     */
    @Override
    public <FK_ENTITY> JsonEncoder<ENTITY> put(
            String label, 
            Finder<ENTITY, FK_ENTITY> finder, 
            JsonEncoder<FK_ENTITY> fkEncoder) {
        
        requireNonNulls(label, finder, fkEncoder);
        getters.put(label, e -> "\"" + label + "\":" + 
            fkEncoder.apply(finder.apply(e, fkEncoder.getManager()))
        );
        
        return this;
    }

    /**************************************************************************/
    /*                        Put Labels with Find Many                       */
    /**************************************************************************/
    
    /**
     * {@inheritDoc}
     */
    @Override
    public <FK_ENTITY> JsonEncoder<ENTITY> putStreamer(
            String label, 
            Function<ENTITY, Stream<FK_ENTITY>> streamer, 
            JsonEncoder<FK_ENTITY> fkEncoder) {
        
        requireNonNulls(label, streamer, fkEncoder);
        getters.put(label, e -> "\"" + label + "\":[" + 
            streamer.apply(e).map(fkEncoder::apply).collect(joining(",")) + 
            "]"
        );
        
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <FK_ENTITY> JsonEncoder<ENTITY> putStreamer(
            String label, 
            Function<ENTITY, Stream<FK_ENTITY>> streamer, 
            Function<FK_ENTITY, String> fkEncoder) {
        
        requireNonNulls(label, streamer, fkEncoder);
        getters.put(label, e -> "\"" + label + "\":[" + 
            streamer.apply(e).map(fkEncoder).collect(joining(",")) +
            "]"
        );
        
        return this;
    }

    /**************************************************************************/
    /*                             Remove by Label                            */
    /**************************************************************************/
    
    /**
     * {@inheritDoc}
     */
    @Override
    public JsonEncoder<ENTITY> remove(String label) {
        requireNonNull(label);
        getters.remove(label);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonEncoder<ENTITY> remove(Field<ENTITY> field) {
        requireNonNull(field);
        getters.remove(jsonField(project, field.identifier()));
        return this;
    }

    /**************************************************************************/
    /*                                  Encode                                */
    /**************************************************************************/
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String apply(ENTITY entity) {
        return entity == null ? "null" : "{"
            + getters.values().stream()
            .map(g -> g.apply(entity))
            .collect(joining(","))
            + "}";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonCollector<ENTITY> collector() {
        return JsonCollector.toJson(this);
    }

    /**************************************************************************/
    /*                  Protected and Private Helper Methods                  */
    /**************************************************************************/

    /**
     * Parse the specified value into JSON.
     * 
     * @param value  the value
     * @return       the JSON encoded value
     */
    private static String jsonValue(byte value) {
        return String.valueOf(value);
    }
    
    /**
     * Parse the specified value into JSON.
     * 
     * @param value  the value
     * @return       the JSON encoded value
     */
    private static String jsonValue(short value) {
        return String.valueOf(value);
    }
    
    /**
     * Parse the specified value into JSON.
     * 
     * @param value  the value
     * @return       the JSON encoded value
     */
    private static String jsonValue(int value) {
        return String.valueOf(value);
    }
    
    /**
     * Parse the specified value into JSON.
     * 
     * @param value  the value
     * @return       the JSON encoded value
     */
    private static String jsonValue(long value) {
        return String.valueOf(value);
    }
    
    /**
     * Parse the specified value into JSON.
     * 
     * @param value  the value
     * @return       the JSON encoded value
     */
    private static String jsonValue(float value) {
        return String.valueOf(value);
    }
    
    /**
     * Parse the specified value into JSON.
     * 
     * @param value  the value
     * @return       the JSON encoded value
     */
    private static String jsonValue(double value) {
        return String.valueOf(value);
    }
    
    /**
     * Parse the specified value into JSON.
     * 
     * @param value  the value
     * @return       the JSON encoded value
     */
    private static String jsonValue(char value) {
        return String.valueOf(value);
    }
    
    /**
     * Parse the specified value into JSON.
     * 
     * @param value  the value
     * @return       the JSON encoded value
     */
    private static String jsonValue(boolean value) {
        return String.valueOf(value);
    }

    /**
     * Parse the specified value into JSON.
     * 
     * @param in  the value
     * @return    the JSON encoded value
     */
    private static String jsonValue(Object in) {
        // in is nullable, a field can certainly be null
        final String value;

        if (in instanceof Optional<?>) {
            final Optional<?> o = (Optional<?>) in;
            return o.map(JsonEncoderImpl::jsonValue).orElse("null");
        } else if (in == null) {
            value = "null";
        } else if (in instanceof Byte
            || in instanceof Short
            || in instanceof Integer
            || in instanceof Long
            || in instanceof Boolean
            || in instanceof Float
            || in instanceof Double) {
            value = String.valueOf(in);
        } else {
            value = "\"" + String.valueOf(in).replace("\"", "\\\"") + "\"";
        }

        return value;
    }
}
