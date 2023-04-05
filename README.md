[![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/htrc/HTRC-MetadataService/ci.yml?branch=develop)](https://github.com/htrc/HTRC-MetadataService/actions/workflows/ci.yml)
[![codecov](https://codecov.io/github/htrc/HTRC-MetadataService/branch/develop/graph/badge.svg?token=LILLTB9G4K)](https://codecov.io/github/htrc/HTRC-MetadataService)
[![GitHub release (latest SemVer including pre-releases)](https://img.shields.io/github/v/release/htrc/HTRC-MetadataService?include_prereleases&sort=semver)](https://github.com/htrc/HTRC-MetadataService/releases/latest)

# HTRC-MetadataService
A simple service for retrieving metadata for a given set of volume IDs

# Build
`sbt clean stage`
Then find the result in `target/universal/stage/`

# Deploy
Copy the folder `target/universal/stage/` to the deployment location and rename as desired (henceforth referred to as `DEPLOY_DIR`).

# Docker
You can create a Docker image with:
`sbt docker:publishLocal`

For more Docker capabilities, see: https://www.scala-sbt.org/sbt-native-packager/formats/docker.html#tasks

# Setup
1. Generate an application secret by running `sbt playGenerateSecret`
2. Set `MONGODB_URI` environment variable to point to the Mongo instance holding the metadata
3. Set `METASERVICE_SECRET` environment variable to the value generated by step 0

# Run
*Note:* You must have the environment variables set before running (or edited the `application.conf` accordingly)
```bash
$DEPLOY_DIR/bin/htrc-metadataservice -Dhttp.address=HOST -Dhttp.port=PORT -Dplay.http.context=/api
```
where `HOST` is the desired hostname or IP to bind to, and `PORT` is the desired port to run on.

# API

## Request format
```
GET   /api/v2/metadata?ids=ID1|ID2|ID3...
POST  /api/v2/metadata
      where the body contains:
      ID1|ID2|ID3|...
      or
      ID1
      ID2
      ID3
      ...
```

Note: For the POST request, `Content-type: text/plain` must be set in the request

## Response format

The response will come as `Content-type:text/plain` in the [JSON lines](https://jsonlines.org/) format, 
where each line represents one metadata record. Any volume IDs requested that aren't found will be ignored
(only the found ones are returned).

Note: The service respects the HTTP `Accept` header.
      The service can return responses in GZIP format by setting `Accept-Encoding: gzip` request header.

# Example

`curl -v -X GET 'http://HOSTNAME:PORT/api/v2/metadata?ids=hvd.32044038401683|SOMEMISSINGID'`

Returns:
```
{ ... metadata record for hvd.32044038401683 ... }
```
(note that `SOMEMISSINGID` is not flagged as missing in the response)
