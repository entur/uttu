#!/bin/bash

# -- > Create S3 bucket for blob storage
awslocal s3api create-bucket --bucket 'local-blobstore-exports' --create-bucket-configuration LocationConstraint=eu-north-1
