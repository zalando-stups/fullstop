# Whitelisting of Violations

Fullstop shows violations when users break the predefined rules. However, there are always cases when things have to be 
done differently, be it for technical or strategical reasons.

To avoid "hacking" all these edge cases into the violation-checking plugins, there is a central whitelisting engine,
that post-processes each violation to decide if it's a known edge case.

Whitelisted violations are marked and can easily be filtered out, before presenting them to the users.

## Whitelisting rules

The whitelisting engine consists of a configurable set of rules. One rule contains multiple matching criteria,
that are applied to violation objects. If and only if **all** checks within one rule yield true, the whitelisting was successful.
Each rule is immutable, which means, the checks within the rule cannot be changed. However rules have a modifiable
expiration date. For example: A feature team and the IT compliance team agreed upon a temporary workaround, that would
result in Fullstop showing violations, a whitelisting rule can be created with an expiration date set. In case the
exact expiration date is not yet known, a rule can also be invalidated at any later point in time. This is the only
possible modification to an existing rule. Updating the checks of a rule, will result in the old one being invalidated
and a new rule with updated check criteria being created. This way, traceability of whitelisting decisions is guaranteed.

## Whitelisting criteria

One rule can have multiple criteria. Only if all of them are met, the rule is applied successfully.

### account_id
ID (12-digit) of an AWS account.
Matches if the violation occurred in this AWS account.

Example: `"111222333444"` 

### application_id
ID of an application, as known from the Kio application registry (usually only lower case chars).
Matches if the violation could be assigned to this application.

Example: `"pierone"`

### application_version
Version of an application, as specified in the Taupage YAML.
Matches if the violation could be assigned to this application version. 

Example: `"v1"`

### image_name
Regular expression.
Matches if the Amazon Machine Image (AMI) name matches the regex.

Example: `"^CoreOS-stable-[0-9.]+-hvm$"`

### image_owner
ID (12-digit) of an AWS account.
Matches if the owner of the AMI is exactly this AWS account.

Example: `"111222333444"`

### meta_info_json_path
[JsonPath](https://github.com/json-path/JsonPath) expression, which should return a list.
Matches if, when applied to the the meta_info property (which is an arbitrary JSON object),
returns a non-empty list of results. JsonPath allows for complex queries.

Example: `"$.[?(@.role_name == 'MyReadOnlyRole' && @.grantees == ['arn:aws:iam::999888777666:root'])]"`

When this expression is applied to the sample meta info:
```json
{
  "role_name": "MyReadOnlyRole",
  "grantees": [
    "999888777666"
  ],
  "role_arn": "arn:aws:iam::111222333444:role/MyReadOnlyRole"
}
```
it will return the result:
```json
[
   {
      "role_name" : "MyReadOnlyRole",
      "grantees" : [
         "arn:aws:iam::999888777666:root"
      ],
      "role_arn" : "arn:aws:iam::111222333444:role/MyReadOnlyRole"
   }
]
```
When we change the expression slightly, e.g. to match for another role name:
`"$.[?(@.role_name == 'AdminRole' && @.grantees == ['arn:aws:iam::999888777666:root'])]"`
and apply to the sample meta info, it will return an empty list `[]`.

**Hint:** Use the [Jayway JsonPath Evaluator](http://jsonpath.herokuapp.com/)
to try out the expressions before using them in a rule.

### region
ID of an AWS region (see [AWS docs](http://docs.aws.amazon.com/general/latest/gr/rande.html) for a complete list).
Matches if the violation occurred in this region.

Example: `"eu-central-1"`

### violation_type
ID of the violation type (use command line tool `fullstop types` to get a complete list).
Matches if the violation was of exactly this type.

Example: `"UNSECURED_PUBLIC_ENDPOINT"`

## Examples

This section shows some example payloads used to create whitelisting rules through the API.

### Whitelist an AMI by name
Allow AWS account `111222333444` to use any CoreOS-stable hvm AMI. Note that the AMI name is matched by a regular
expression. For additional security, the AMI owner needs to match as well.
```json
  {
    "account_id": "111222333444",
    "image_name": "^CoreOS-stable-[0-9.]+-hvm$",
    "image_owner": "595879546273",
    "reason": "As requested and approved in ticket #4711",
    "violation_type_entity_id": "WRONG_AMI"
  }
```

### Whitelist OUTDATED_TAUPAGE
Allow AWS account `111222333444` to pause updating the Taupage AMI to the latest version for the application `my-app`
until end of Q1 2018. 
```json
  {
    "account_id": "111222333444",
    "application_id": "my-app",
    "reason": "As requested and approved in ticket #4711",
    "expiry_date": "2018-04-30T00:00:00.000Z",
    "violation_type_entity_id": "OUTDATED_TAUPAGE"
  }
```
