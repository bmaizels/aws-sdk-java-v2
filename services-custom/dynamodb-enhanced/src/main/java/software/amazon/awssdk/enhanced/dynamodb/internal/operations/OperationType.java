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

package software.amazon.awssdk.enhanced.dynamodb.internal.operations;

import software.amazon.awssdk.annotations.SdkInternalApi;

@SdkInternalApi
public enum OperationType {
    ANY("Any"), //unknown value
    BATCH_GET_ITEM("BatchGetItemOperation"),
    BATCH_WRITE_ITEM("BatchWriteItemOperation"),
    CREATE_TABLE("CreateTableOperation"),
    DELETE_ITEM("DeleteItemOperation"),
    DELETE_TABLE("DeleteTableOperation"),
    GET_ITEM("GetItemOperation"),
    QUERY("QueryOperation"),
    PUT_ITEM("PutItemOperation"),
    SCAN("ScanOperation"),
    TRANSACT_GET_ITEMS("TransactGetItemsOperation"),
    TRANSACT_WRITE_ITEMS("TransactWriteItemsOperation"),
    UPDATE_ITEM("UpdateItemOperation");

    private final String label;

    OperationType() {
        this.label = null;
    }

    OperationType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
