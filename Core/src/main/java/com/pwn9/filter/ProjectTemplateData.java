package com.pwn9.filter;

import com.sun.xml.internal.bind.v2.model.core.ID;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 24/08/2020.
 */
public final class ProjectTemplateData {
    public static final String ID =  "${project.parent.name}";
    public static final String VERSION =  "${project.version}";
    public static final String DESCRIPTION =  "${project.version}";
    public static final String URL = "${project.parent.url}";
    public static final String[] AUTHORS = {"Sage","Narimm","Tremor77"};
}
