package scot.mygov.geosearch.api.models;

public class Postcode  {

    private String postcode;

    private String district;

    private String normalisedPostcode;

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getNormalisedPostcode() {
        return normalisedPostcode;
    }

    public void setNormalisedPostcode(String normalisedPostcode) {
        this.normalisedPostcode = normalisedPostcode;
    }
}
