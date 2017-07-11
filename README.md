# MyGov GeoSearch Service

A REST service to provide local authority information given a Scottish postcode.

This project contains the REST service only, it does not contain any address
data. It uses the Ordnance Survey Code Point data set, which has a different
release cadence and is maintained separately. See the os-codepoint repository
for an extract of the Code Point data set that contains information about
Scottish postcodes.

This project has a Maven dependency on the os-codepoint artefact for
development purposes. The Debian package built by this project has a
dependency on the Debian package built by the os-codepoint project.

# Build and deploy

* Run `mvn install` on the os-codepoint repository.
* Run `mvn install` on this repository.
* Install the os-codepoint Debian package.
* Install the Debian package built by the geo-deb project.

# Monitoring

The healthcheck endpoint is `GET /health`. The endpoint returns a JSON response
with the properties listed below. The status code is `200` if the service is
healthy, and `503` otherwise.

* `copyrightDate`
  * Indicates the copyright date on the Codepoint data set in use.
  * Type: string (yyyy-MM-dd)
* `status`
  * Indicates the status of the service, which depends on whether the Codepoint data is recent.
  * Type: string (OK, WARNING)
* `message`
  * A status message to be shown if the service is healthy.
  * Type: string

The `/health` endpoint supports the following optional parameters:

* `grace`
  * The grace period to allow before warning that the Codepoint data set is out of date.
    This must be a a period string in the ISO-8601 based format used by java.time.Period.parse().
  * Type: string
  * Default: P1M

# Usage

## Search by postcode

GET [http://localhost:9092/postcodes/{postcode}]

Replace {postcode} with a valid Scottish postcode.

```json
{
  "postcode" : "ML20AR",
  "district" : "S12000044",
}
```

The local authority can be determined by looking up the district identifier in
the list of 'Unitary Authorities' in the Codelist.xls file in the Code Point
data set.
