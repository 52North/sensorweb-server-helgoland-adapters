package org.n52.sensorweb.server.helgoland.adapters.connector.mapping;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "phenomenonTime",
        "resultTime",
        "result",
        "resultQuality",
        "validTime",
        "parameters"
})
public class Observation implements Serializable, Entity {
    private final static long serialVersionUID = -1131449219115560395L;

    @JsonProperty("phenomenonTime")
    private String phenomenonTime;
    @JsonProperty("resultTime")
    private String resultTime;
    @JsonProperty("result")
    private String result;
    @JsonProperty("resultQuality")
    private String resultQuality;
    @JsonProperty("validTime")
    private String validTime;
    @JsonProperty("parameters")
    private List<String> parameters = new ArrayList<String>();

    @JsonProperty("phenomenonTime")
    public String getPhenomenonTime() {
        return phenomenonTime;
    }

    @JsonProperty("phenomenonTime")
    public void setPhenomenonTime(String phenomenonTime) {
        this.phenomenonTime = phenomenonTime;
    }

    @JsonProperty("resultTime")
    public String getResultTime() {
        return resultTime;
    }

    @JsonProperty("resultTime")
    public void setResultTime(String resultTime) {
        this.resultTime = resultTime;
    }

    @JsonProperty("result")
    public String getResult() {
        return result;
    }

    @JsonProperty("result")
    public void setResult(String result) {
        this.result = result;
    }

    @JsonProperty("resultQuality")
    public String getResultQuality() {
        return resultQuality;
    }

    @JsonProperty("resultQuality")
    public void setResultQuality(String resultQuality) {
        this.resultQuality = resultQuality;
    }

    @JsonProperty("validTime")
    public String getValidTime() {
        return validTime;
    }

    @JsonProperty("validTime")
    public void setValidTime(String validTime) {
        this.validTime = validTime;
    }

    @JsonProperty("parameters")
    public List<String> getParameters() {
        return parameters;
    }

    @JsonProperty("parameters")
    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

    @Override
    public Set<String> getFields() {
        Set<String> fields = new HashSet<>();
        add(fields, getPhenomenonTime());
        add(fields, getResult());
        add(fields, getValidTime());
        add(fields, getResultTime());

        getParameters().forEach(prop -> add(fields, prop));
        return fields;
    }

    protected Set<String> add(Set<String> fields, String field) {
        if (field != null && !field.isEmpty()) {
            fields.add(field);
        }
        return fields;
    }

}
