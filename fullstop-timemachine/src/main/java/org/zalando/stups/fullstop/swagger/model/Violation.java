/**
 * Copyright 2015 Zalando SE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.zalando.stups.fullstop.swagger.model;


import com.wordnik.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;


@ApiModel(description = "")
public class Violation  {
  
  private String eventId = null;
  private String accountId = null;
  private String region = null;
  private String message = null;
  private Object violationObject = null;
  private String comment = null;
  private Boolean checked = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("eventId")
  public String getEventId() {
    return eventId;
  }
  public void setEventId(String eventId) {
    this.eventId = eventId;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("accountId")
  public String getAccountId() {
    return accountId;
  }
  public void setAccountId(String accountId) {
    this.accountId = accountId;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("region")
  public String getRegion() {
    return region;
  }
  public void setRegion(String region) {
    this.region = region;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("message")
  public String getMessage() {
    return message;
  }
  public void setMessage(String message) {
    this.message = message;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("violationObject")
  public Object getViolationObject() {
    return violationObject;
  }
  public void setViolationObject(Object violationObject) {
    this.violationObject = violationObject;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("comment")
  public String getComment() {
    return comment;
  }
  public void setComment(String comment) {
    this.comment = comment;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("checked")
  public Boolean getChecked() {
    return checked;
  }
  public void setChecked(Boolean checked) {
    this.checked = checked;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class Violation {\n");
    
    sb.append("  eventId: ").append(eventId).append("\n");
    sb.append("  accountId: ").append(accountId).append("\n");
    sb.append("  region: ").append(region).append("\n");
    sb.append("  message: ").append(message).append("\n");
    sb.append("  violationObject: ").append(violationObject).append("\n");
    sb.append("  comment: ").append(comment).append("\n");
    sb.append("  checked: ").append(checked).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
