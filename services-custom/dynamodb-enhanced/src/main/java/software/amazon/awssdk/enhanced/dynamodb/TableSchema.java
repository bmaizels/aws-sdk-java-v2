/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.awssdk.enhanced.dynamodb;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import software.amazon.awssdk.annotations.SdkPublicApi;
import software.amazon.awssdk.enhanced.dynamodb.mapper.BeanTableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.ImmutableTableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticImmutableTableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticTableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.TableSchemaFactory;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPreserveEmptyObject;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

/**
 * Interface for a mapper that is capable of mapping a modelled Java object into a map of {@link AttributeValue} that is
 * understood by the DynamoDb low-level SDK and back again. This object is also expected to know about the
 * structure of the table it is modelling, which is stored in a {@link TableMetadata} object.
 *
 * @param <T> The type of model object that is being mapped to records in the DynamoDb table.
 */
@SdkPublicApi
public interface TableSchema<T> {
    /**
     * Returns a builder for the {@link StaticTableSchema} implementation of this interface which allows all attributes,
     * tags and table structure to be directly declared in the builder.
     *
     * @param itemClass The class of the item this {@link TableSchema} will map records to.
     * @param <T> The type of the item this {@link TableSchema} will map records to.
     * @return A newly initialized {@link StaticTableSchema.Builder}.
     */
    static <T> StaticTableSchema.Builder<T> builder(Class<T> itemClass) {
        return StaticTableSchema.builder(itemClass);
    }

    /**
     * Returns a builder for the {@link StaticImmutableTableSchema} implementation of this interface which allows all
     * attributes, tags and table structure to be directly declared in the builder.
     *
     * @param immutableItemClass The class of the immutable item this {@link TableSchema} will map records to.
     * @param immutableBuilderClass The class that can be used to construct immutable items this {@link TableSchema}
     *                              maps records to.
     * @param <T> The type of the immutable item this {@link TableSchema} will map records to.
     * @param <B> The type of the builder used by this {@link TableSchema} to construct immutable items with.
     * @return A newly initialized {@link StaticImmutableTableSchema.Builder}
     */
    static <T, B> StaticImmutableTableSchema.Builder<T, B> builder(Class<T> immutableItemClass,
                                                                   Class<B> immutableBuilderClass) {
        return StaticImmutableTableSchema.builder(immutableItemClass, immutableBuilderClass);
    }

    /**
     * Scans a bean class that has been annotated with DynamoDb bean annotations and then returns a
     * {@link BeanTableSchema} implementation of this interface that can map records to and from items of that bean
     * class.
     *
     * Creating a {@link BeanTableSchema} is a moderately expensive operation, and should be performed sparingly. This is
     * usually done once at application startup.
     *
     * @param beanClass The bean class this {@link TableSchema} will map records to.
     * @param <T> The type of the item this {@link TableSchema} will map records to.
     * @return An initialized {@link BeanTableSchema}.
     */
    static <T> BeanTableSchema<T> fromBean(Class<T> beanClass) {
        return BeanTableSchema.create(beanClass);
    }

    /**
     * Scans an immutable class that has been annotated with DynamoDb immutable annotations and then returns a
     * {@link ImmutableTableSchema} implementation of this interface that can map records to and from items of that
     * immutable class.
     *
     * Creating a {@link ImmutableTableSchema} is a moderately expensive operation, and should be performed sparingly. This is
     * usually done once at application startup.
     *
     * @param immutableClass The immutable class this {@link TableSchema} will map records to.
     * @param <T> The type of the item this {@link TableSchema} will map records to.
     * @return An initialized {@link ImmutableTableSchema}.
     */
    static <T> ImmutableTableSchema<T> fromImmutableClass(Class<T> immutableClass) {
        return ImmutableTableSchema.create(immutableClass);
    }

    /**
     * Scans a class that has been annotated with DynamoDb enhanced client annotations and then returns an appropriate
     * {@link TableSchema} implementation that can map records to and from items of that class. Currently supported
     * top level annotations (see documentation on those classes for more information on how to use them):
     * <p>
     * {@link software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean}<br>
     * {@link software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbImmutable}
     *
     * This is a moderately expensive operation, and should be performed sparingly. This is usually done once at
     * application startup.
     *
     * @param annotatedClass A class that has been annotated with DynamoDb enhanced client annotations.
     * @param <T> The type of the item this {@link TableSchema} will map records to.
     * @return An initialized {@link TableSchema}
     */
    static <T> TableSchema<T> fromClass(Class<T> annotatedClass) {
        return TableSchemaFactory.fromClass(annotatedClass);
    }

    /**
     * Takes a raw DynamoDb SDK representation of a record in a table and maps it to a Java object. A new object is
     * created to fulfil this operation.
     * <p>
     * If attributes are missing from the map, that will not cause an error, however if attributes are found in the
     * map which the mapper does not know how to map, an exception will be thrown.
     *
     * <p>
     * If all attribute values in the attributeMap are null, null will be returned. Use {@link #mapToItem(Map, boolean)}
     * instead if you need to preserve empty object.
     *
     * <p>
     * If the implementation supports polymorphic mapping, the context of the attribute map will be used to determine
     * the correct subtype of the object returned.
     * <p>
     * API Implementors Note:
     * <p>
     * {@link #mapToItem(Map, boolean)} must be implemented if {@code preserveEmptyObject} behavior is desired.
     *
     * @param attributeMap A map of String to {@link AttributeValue} that contains all the raw attributes to map.
     * @return A new instance of a Java object with all the attributes mapped onto it.
     * @throws IllegalArgumentException if any attributes in the map could not be mapped onto the new model object.
     * @see #mapToItem(Map, boolean)
     */
    T mapToItem(Map<String, AttributeValue> attributeMap);

    /**
     * Takes a raw DynamoDb SDK representation of a record in a table and maps it to a Java object. A new object is
     * created to fulfil this operation.
     * <p>
     * If attributes are missing from the map, that will not cause an error, however if attributes are found in the
     * map which the mapper does not know how to map, an exception will be thrown.
     *
     * <p>
     * In the scenario where all attribute values in the map are null, it will return null if {@code preserveEmptyObject}
     * is true. If it's false, an empty object will be returned.
     *
     * <p>
     * Note that {@code preserveEmptyObject} only applies to the top level Java object, if it has nested "empty" objects, they
     * will be mapped as null. You can use {@link DynamoDbPreserveEmptyObject} to configure this behavior for nested objects.
     *
     * <p>
     * If the implementation supports polymorphic mapping, the context of the attribute map will be used to determine
     * the correct subtype of the object returned.
     *
     * <p>
     * API Implementors Note:
     * <p>
     * This method must be implemented if {@code preserveEmptyObject} behavior is to be supported
     *
     * @param attributeMap A map of String to {@link AttributeValue} that contains all the raw attributes to map.
     * @param preserveEmptyObject whether to initialize this Java object as empty class if all fields are null
     * @return A new instance of a Java object with all the attributes mapped onto it.
     * @throws IllegalArgumentException if any attributes in the map could not be mapped onto the new model object.
     * @throws UnsupportedOperationException if {@code preserveEmptyObject} is not supported in the implementation
     * @see #mapToItem(Map)
     */
    default T mapToItem(Map<String, AttributeValue> attributeMap, boolean preserveEmptyObject) {
        if (preserveEmptyObject) {
            throw new UnsupportedOperationException("preserveEmptyObject is not supported. You can set preserveEmptyObject to "
                                                    + "false to continue to call this operation. If you wish to enable "
                                                    + "preserveEmptyObject, please reach out to the maintainers of the "
                                                    + "implementation class for assistance.");
        }
        return mapToItem(attributeMap);
    }

    /**
     * Takes a modelled object and converts it into a raw map of {@link AttributeValue} that the DynamoDb low-level
     * SDK can work with.
     * <p>
     * If the implementation supports polymorphic mapping, the context of the item will be used to determine the correct
     * subtype schema of the returned map.
     *
     * @param item The modelled Java object to convert into a map of attributes.
     * @param ignoreNulls If set to true; any null values in the Java object will not be added to the output map.
     *                    If set to false; null values in the Java object will be added as {@link AttributeValue} of
     *                    type 'nul' to the output map.
     * @return A map of String to {@link AttributeValue} representing all the modelled attributes in the model object.
     */
    Map<String, AttributeValue> itemToMap(T item, boolean ignoreNulls);

    /**
     * Takes a modelled object and extracts a specific set of attributes which are then returned as a map of
     * {@link AttributeValue} that the DynamoDb low-level SDK can work with. This method is typically used to extract
     * just the key attributes of a modelled item and will not ignore nulls on the modelled object.
     * <p>
     * If the implementation supports polymorphic mapping, the context of the item will be used to determine the correct
     * subtype schema of the returned map.
     *
     * @param item The modelled Java object to extract the map of attributes from.
     * @param attributes A collection of attribute names to extract into the output map.
     * @return A map of String to {@link AttributeValue} representing the requested modelled attributes in the model
     * object.
     */
    Map<String, AttributeValue> itemToMap(T item, Collection<String> attributes);

    /**
     * Returns a single attribute value from the modelled object.
     *
     * @param item The modelled Java object to extract the attribute from.
     * @param attributeName The attribute name describing which attribute to extract.
     * @return A single {@link AttributeValue} representing the requested modelled attribute in the model object or
     * null if the attribute has not been set with a value in the modelled object.
     */
    AttributeValue attributeValue(T item, String attributeName);

    /**
     * Returns the object that describes the structure of the table being modelled by the mapper. This includes
     * information such as the table name, index keys and attribute tags.
     * @return A {@link TableMetadata} object that contains structural information about the table being modelled.
     */
    TableMetadata tableMetadata();

    /**
     * Returns the {@link EnhancedType} that represents the 'Type' of the Java object this table schema object maps to
     * and from.
     * @return The {@link EnhancedType} of the modelled item this TableSchema maps to.
     */
    EnhancedType<T> itemType();

    /**
     * Returns a complete list of attribute names that are mapped by this {@link TableSchema}
     */
    List<String> attributeNames();

    /**
     * A boolean value that represents whether this {@link TableSchema} is abstract which means that it cannot be used
     * to directly create records as it is lacking required structural elements to map to a table, such as a primary
     * key, but can be referred to and embedded by other schemata.
     *
     * @return true if it is abstract, and therefore cannot be used directly to create records but can be referred to
     * by other schemata, and false if it is concrete and may be used to map records directly.
     */
    boolean isAbstract();

    /**
     * {@link AttributeConverter} that is applied to the given key.
     *
     * @param key Attribute of the modelled item.
     * @return AttributeConverter defined for the given attribute key.
     */
    default AttributeConverter<T> converterForAttribute(Object key) {
        throw new UnsupportedOperationException();
    }

    /**
     * If applicable, returns a {@link TableSchema} for a specific object subtype. If the implementation does not
     * support polymorphic mapping, then this method will, by default, return the current instance. This method is
     * primarily used to pass the right contextual information to extensions when they are invoked mid-operation. This
     * method is not required to get a polymorphic {@link TableSchema} to correctly map subtype objects using
     * 'mapToItem' or 'itemToMap'.
     * @param itemContext the subtype object to retrieve the subtype {@link TableSchema} for.
     * @return the subtype {@link TableSchema} or the current {@link TableSchema} if subtypes are not supported.
     */
    default TableSchema<? extends T> subtypeTableSchema(T itemContext) {
        return this;
    }

    /**
     * If applicable, returns a {@link TableSchema} for a specific object subtype. If the implementation does not
     * support polymorphic mapping, then this method will, by default, return the current instance. This method is
     * primarily used to pass the right contextual information to extensions when they are invoked mid-operation. This
     * method is not required to get a polymorphic {@link TableSchema} to correctly map subtype objects using
     * 'mapToItem' or 'itemToMap'.
     * @param itemContext the subtype object map to retrieve the subtype {@link TableSchema} for.
     * @return the subtype {@link TableSchema} or the current {@link TableSchema} if subtypes are not supported.
     */
    default TableSchema<? extends T> subtypeTableSchema(Map<String, AttributeValue> itemContext) {
        return this;
    }
}
