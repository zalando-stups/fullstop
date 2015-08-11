# from pprint import pprint
from boto3.session import Session
import boto3
import time

separator = ';'

accounts = [
    '1234566789',
    '9886542323']

for accountid in accounts:

    # print('######## '+ accountid +' ########')

    client = boto3.client('sts')
    response = client.assume_role(
        RoleArn='arn:aws:iam::' + accountid + ':role/fullstop',
        RoleSessionName='fullstop',
        DurationSeconds=900
    )

    # pprint(response)

    # print(response['Credentials']['AccessKeyId'])
    # print(response['Credentials']['SecretAccessKey'])
    # print(response['Credentials']['SessionToken'])

    session = Session(aws_access_key_id=response['Credentials']['AccessKeyId'],
                      aws_secret_access_key=response['Credentials']['SecretAccessKey'],
                      aws_session_token=response['Credentials']['SessionToken']
                      )

    ec2 = session.resource('ec2', region_name='eu-west-1')
    ec2_eu_central_1 = session.resource('ec2', region_name='eu-central-1')

    # Boto 3
    # Use the filter() method of the instances collection to retrieve
    # all running EC2 instances.
    # print('######## eu-west-1 ########')
    instances = ec2.instances.filter(
        Filters=[{'Name': 'instance-state-name', 'Values': ['running']}])
    for instance in instances:
        image = ec2.Image(instance.image_id)
        if image and hasattr(image, 'name'): # if image is not None or not image or not image.name
            print(accountid + separator +
                  'eu-west-1' + separator +
                  instance.id + separator +
                  instance.image_id + separator +
                  image.name)
        else:
            print(accountid + separator +
                  'eu-west-1' + separator +
                  instance.id + separator +
                  instance.image_id)
        # break # remove this to list all instances

    time.sleep(5)

    # print('######## eu-central-1 ########')
    instances = ec2_eu_central_1.instances.filter(
        Filters=[{'Name': 'instance-state-name', 'Values': ['running']}])
    for instance in instances:
        image = ec2_eu_central_1.Image(instance.image_id)
        if image and hasattr(image, 'name'): # if image is not None or not image or not image.name
            print(accountid + separator +
                  'eu-central-1' + separator +
                  instance.id + separator +
                  instance.image_id + separator +
                  image.name)
        else:
            print(accountid + separator +
                  'eu-central-1' + separator +
                  instance.id + separator +
                  instance.image_id)
    # break

    time.sleep(5)
