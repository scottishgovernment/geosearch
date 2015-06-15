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

## Usage

### Build and deploy

* Run `mvn install` on the os-codepoint repository.
* Run `mvn install` on this repository.
* Install the os-codepoint Debian package.
* Install the Debian package built by the geo-deb project.

### Search by postcode

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
