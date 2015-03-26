/*
 * Copyright 2015 Zalando SE
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.zalando.stups;

import java.util.ArrayList;
import java.util.List;

public class LogFiles {

    public static final String LOG_FILE_1 = "089972051332_CloudTrail_eu-west-1_20150313T0825Z_lrDYcRZCYinpUuVQ.json.gz";
    public static final String LOG_FILE_2 = "089972051332_CloudTrail_eu-west-1_20150313T0830Z_2Q0baZi1wL4s5q1x.json.gz";
    public static final String LOG_FILE_3 = "089972051332_CloudTrail_eu-west-1_20150313T0840Z_aNeHyPOzW5tEQkIY.json.gz";
    public static final String LOG_FILE_4 = "089972051332_CloudTrail_eu-west-1_20150313T0855Z_ELT1jm2JgtAALS35.json.gz";
    public static final String LOG_FILE_5 = "089972051332_CloudTrail_eu-west-1_20150313T0910Z_EN6JQL6qlAeAUpa5.json.gz";
    public static final String LOG_FILE_6 = "089972051332_CloudTrail_eu-west-1_20150313T0925Z_44COEkgkoaG9ySuD.json.gz";
    public static final String LOG_FILE_7 = "089972051332_CloudTrail_eu-west-1_20150313T0930Z_9oSVGT5GkQN0sbVn.json.gz";
    public static final String LOG_FILE_8 = "089972051332_CloudTrail_eu-west-1_20150313T0940Z_tQ1TFvH8wmi1SVnz.json.gz";
    public static final String LOG_FILE_9 = "089972051332_CloudTrail_eu-west-1_20150313T0955Z_cL8cQkj2VihJ7Ylc.json.gz";

    private static final List<String> logFiles = new ArrayList<>();

    static {
        logFiles.add(LOG_FILE_1);
        logFiles.add(LOG_FILE_2);
        logFiles.add(LOG_FILE_3);
        logFiles.add(LOG_FILE_4);
        logFiles.add(LOG_FILE_5);
        logFiles.add(LOG_FILE_6);
        logFiles.add(LOG_FILE_7);
        logFiles.add(LOG_FILE_8);
        logFiles.add(LOG_FILE_9);
    }

    public static List<String> all() {
        return logFiles;
    }
}