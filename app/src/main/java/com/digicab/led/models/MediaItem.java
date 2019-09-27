package com.digicab.led.models;

import in.cubestack.android.lib.storm.FieldType;
import in.cubestack.android.lib.storm.annotation.Column;
import in.cubestack.android.lib.storm.annotation.PrimaryKey;
import in.cubestack.android.lib.storm.annotation.Table;

@Table(name = "MediaTable")
public class MediaItem  {

    /**
     * id : 6
     * ad_type : 1
     * upload : 1560257974CentredImage,426468,en.jpg
     * location_id : 1
     */

    @PrimaryKey
    @Column(name = "primary_id", type = FieldType.INTEGER)
    public int primary_id;

    @Column(name = "id", type = FieldType.INTEGER)
    public int id;

    @Column(name = "location_id", type = FieldType.INTEGER)
    public int location_id;

    @Column(name = "ad_type", type = FieldType.INTEGER)
    public int ad_type;

    @Column(name = "upload", type = FieldType.TEXT)
    public String upload;

    @Column(name = "download", type = FieldType.TEXT)
    public String download;

}
