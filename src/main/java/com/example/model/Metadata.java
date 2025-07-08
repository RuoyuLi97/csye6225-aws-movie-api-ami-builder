package com.example.model;

import lombok.Data;

@Data
public class Metadata {
    private String instanceId;
    private String availabilityZone;

    public Metadata(String instanceId, String availabilityZone){
        this.instanceId = instanceId;
        this.availabilityZone = availabilityZone;
    }
}
