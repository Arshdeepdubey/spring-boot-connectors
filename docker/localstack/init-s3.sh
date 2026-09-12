#!/bin/sh
# LocalStack runs every executable script under /etc/localstack/init/ready.d/
# once the container's services are ready - this creates the bucket the
# connectors read/write against so no manual "aws s3 mb" step is needed.
set -e

BUCKET_NAME="${TARGET_S3_BUCKET:-connector-orders-bucket}"

awslocal s3api head-bucket --bucket "$BUCKET_NAME" 2>/dev/null \
  && echo "Bucket $BUCKET_NAME already exists" \
  || awslocal s3 mb "s3://$BUCKET_NAME"

echo "LocalStack S3 bucket ready: $BUCKET_NAME"
