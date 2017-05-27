## What

`Ascii-less-gallery` is a sample project combining [AWS Lambda functions](https://aws.amazon.com/lambda),
[Serverless framework](https://serverless.com/) and [Kotlin language](https://kotlinlang.org/). It was inspired by
[serverless-photo-recognition](https://github.com/awslabs/serverless-photo-recognition) github project. Intention was to try to
setup project that use [AWS Lambda functions](https://aws.amazon.com/lambda) with minimal effort - everything thanks to 
[Serverless framework](https://serverless.com/)

[![Build](https://api.travis-ci.org/ajurasz/ascii-less-gallery.svg)](https://travis-ci.org/ajurasz/ascii-less-gallery/) [![codecov](https://codecov.io/gh/ajurasz/ascii-less-gallery/branch/master/graph/badge.svg)](https://codecov.io/gh/ajurasz/ascii-less-gallery)

### Features

* custom authorization leveraging redis for caching user access tokens - this reduce numbers of request (cost) to AWS DynamoDB
* user login and registration
* image upload and conversion to ascii
* storing labeled images in ElasticSearch - labeling is done using [AWS Rekognition](https://aws.amazon.com/rekognition/) service

### Architecture

![Architecture](architecture.png)

## Prerequisite

1. Create ElasticSearch domain `es-gallery`
2. Create Redis - due to [not being able](https://forums.aws.amazon.com/thread.jspa?messageID=767285) to call Redis from Lambda I decided to use [redislabs](https://redislabs.com/) free plan.
3. One manual tweek is required to make it all work. Login to AWS Management Console and navigate to API Gateway service then on the left side select `dev-ascii-less-gallery-service`
next `Binary support` and add files you want to be supporting like `image/png`

## Installation

1. Install and configure serverless framework ([Instruction](https://serverless.com/framework/docs/providers/aws/guide/installation/))
2. Build project ./gradlew build
3. Run `sls deploy` (I'm using dedicated account for serverless called `sless` and it is set in [serverless.yml](https://github.com/ajurasz/ascii-less-gallery/blob/master/serverless.yml) file). 
When deploy finish successfully all functions URLs will be printed in the console window.

## Usage

1. Register new user:
```shell
curl -X POST \
  https://xxxx/dev/register \
  -H 'content-type: application/json' \
  -d '{"email":"test@test.com", "password":"admin"}'
```

2. Obtain user access token:
```shell
curl -X POST \
  https://xxxx/dev/login \
  -H 'authorization: Basic dGVzdEB0ZXN0LnBsOmFkbWlu'
```

3. Upload image for recognition and conversion to ascii
```shell
curl -X POST \
  https://xxxx/dev/gallery \
  -H 'content-type: image/png' \
  -H 'token: eyJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE0OTQ5NTA0NDgsImlzcyI6InRlc3RAdGVzdC5wbCJ9.Dj0itEWPE2PQ6WbeLgIWgHwslaVLWAgRlj-2scJ8Uk4'
```

4. List all images
```shell
curl -X GET \
  https://xxxx/dev/gallery 
```

5. User can delete his documents:
```shell
curl -X DELETE \
  https://xxxx/dev/gallery/def2c0d8-5d13-4603-83ef-79bf9e07944d \
  -H 'token: eyJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE0OTQ5NTA0NDgsImlzcyI6InRlc3RAdGVzdC5wbCJ9.Dj0itEWPE2PQ6WbeLgIWgHwslaVLWAgRlj-2scJ8Uk4'
```



