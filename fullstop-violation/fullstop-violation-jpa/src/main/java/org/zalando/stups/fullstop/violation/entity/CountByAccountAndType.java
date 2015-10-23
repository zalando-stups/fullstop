/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop.violation.entity;

public class CountByAccountAndType {

    private final String account;
    private final String type;
    private final long quantity;

    public CountByAccountAndType(String account, String type, long quantity) {
        this.account = account;
        this.type = type;
        this.quantity = quantity;
    }

    public String getAccount() {
        return account;
    }

    public String getType() {
        return type;
    }

    public long getQuantity() {
        return quantity;
    }

    @Override
    public String toString() {
        return "{" + "account='" + account + '\'' + ", type='" + type + '\'' + ", quantity=" + quantity + '}';
    }


}
