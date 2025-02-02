-- ------------------------------------------------------------------------
-- This file was generated by hibernate for SMP version 5.1-RC2-SNAPSHOT.
-- ------------------------------------------------------------------------


    alter table SMP_ALERT_AUD 
       drop 
       foreign key FKrw0qnto448ojlirpfmfntd8v2;

    alter table SMP_ALERT_PROPERTY 
       drop 
       foreign key FK15r37w3r5ty5f6074ykr2o4i6;

    alter table SMP_ALERT_PROPERTY_AUD 
       drop 
       foreign key FKod33qjx87ih1a0skxl2sgddar;

    alter table SMP_CERTIFICATE 
       drop 
       foreign key FK25b9apuupvmjp18wnn2b2gfg8;

    alter table SMP_CERTIFICATE_AUD 
       drop 
       foreign key FKnrwm8en8vv10li8ihwnurwd9e;

    alter table SMP_CONFIGURATION_AUD 
       drop 
       foreign key FKd4yhbdlusovfbdti1fjkuxp9m;

    alter table SMP_CREDENTIAL 
       drop 
       foreign key FK89it2lyqvi2bl9bettx66n8n1;

    alter table SMP_CREDENTIAL_AUD 
       drop 
       foreign key FKqjh6vxvb5tg0tvbkvi3k3xhe6;

    alter table SMP_DOCUMENT 
       drop 
       foreign key FKbytp2kp8g3pj8qfp1g6a2g7p;

    alter table SMP_DOCUMENT_AUD 
       drop 
       foreign key FKh9epnme26i271eixtvrpqejvi;

    alter table SMP_DOCUMENT_PROPERTY 
       drop 
       foreign key FKfag3795e9mrvfvesd00yis9yh;

    alter table SMP_DOCUMENT_PROPERTY_AUD 
       drop 
       foreign key FK81057kcrugb1cfm0io5vkxtin;

    alter table SMP_DOCUMENT_VERSION 
       drop 
       foreign key FKalsuoqx4csyp9mygvng911do;

    alter table SMP_DOCUMENT_VERSION_AUD 
       drop 
       foreign key FK4glqiu73939kpyyb6bhw822k3;

    alter table SMP_DOCUMENT_VERSION_EVENT 
       drop 
       foreign key FK6es2svpoxyrnt1h05c9junmdn;

    alter table SMP_DOMAIN_AUD 
       drop 
       foreign key FK35qm8xmi74kfenugeonijodsg;

    alter table SMP_DOMAIN_CONFIGURATION 
       drop 
       foreign key FK4303vstoigqtmeo3t2i034gm3;

    alter table SMP_DOMAIN_CONFIGURATION_AUD 
       drop 
       foreign key FKkelcga805bleh5x256hy5e1xb;

    alter table SMP_DOMAIN_MEMBER 
       drop 
       foreign key FK1tdwy9oiyrk6tl4mk0fakhkf5;

    alter table SMP_DOMAIN_MEMBER 
       drop 
       foreign key FKino2nvj74wc755nyn5mo260qi;

    alter table SMP_DOMAIN_MEMBER_AUD 
       drop 
       foreign key FKijiv1avufqo9iu5u0cj4v3pv7;

    alter table SMP_DOMAIN_RESOURCE_DEF 
       drop 
       foreign key FK563xw5tjw4rlr32va9g17cdsq;

    alter table SMP_DOMAIN_RESOURCE_DEF 
       drop 
       foreign key FKtppp16v40ll2ch3ly8xusb8hi;

    alter table SMP_DOMAIN_RESOURCE_DEF_AUD 
       drop 
       foreign key FKpujj9vb097i5w4loa3dxww2nj;

    alter table SMP_EXTENSION_AUD 
       drop 
       foreign key FKke7f9wbwvp1bmnlqh9hrfm0r;

    alter table SMP_GROUP 
       drop 
       foreign key FKjeomxyxjueaiyt7f0he0ls7vm;

    alter table SMP_GROUP_AUD 
       drop 
       foreign key FKeik3quor2dxho7bmyoxc2ug9o;

    alter table SMP_GROUP_MEMBER 
       drop 
       foreign key FK3y21chrphgx1dytux0p19btxe;

    alter table SMP_GROUP_MEMBER 
       drop 
       foreign key FK8ue5gj1rx6gyiqp19dscp85ut;

    alter table SMP_GROUP_MEMBER_AUD 
       drop 
       foreign key FK5pmorcyhwkaysh0a8xm99x6a8;

    alter table SMP_RESOURCE 
       drop 
       foreign key FKkc5a6okrvq7dv87itfp7i1vmv;

    alter table SMP_RESOURCE 
       drop 
       foreign key FK24mw8fiua39nh8rnobhgmujri;

    alter table SMP_RESOURCE 
       drop 
       foreign key FKft55kasui36i77inf0wh8utv5;

    alter table SMP_RESOURCE_AUD 
       drop 
       foreign key FKlbbfltxw6qmph5w3i8c9qf6kb;

    alter table SMP_RESOURCE_DEF 
       drop 
       foreign key FKruu7v6uig9h333ihv34haw3ob;

    alter table SMP_RESOURCE_DEF_AUD 
       drop 
       foreign key FKapswkgbdm9s4wwhx2cjduoniw;

    alter table SMP_RESOURCE_MEMBER 
       drop 
       foreign key FKrci5jlgnckwo1mhq2rvmfaptw;

    alter table SMP_RESOURCE_MEMBER 
       drop 
       foreign key FKs6jx68jxlx4xfdtxy20f3s6lu;

    alter table SMP_RESOURCE_MEMBER_AUD 
       drop 
       foreign key FKknykp2wcby9fxk234yaaix1pe;

    alter table SMP_SUBRESOURCE 
       drop 
       foreign key FK7y1ydnq350mbs3c8yrq2fhnsk;

    alter table SMP_SUBRESOURCE 
       drop 
       foreign key FK7clbsapruvhkcqgekfxs8prex;

    alter table SMP_SUBRESOURCE 
       drop 
       foreign key FKq3wmyy4ieoenuu1s55237qu9k;

    alter table SMP_SUBRESOURCE_AUD 
       drop 
       foreign key FKffihyo233ldee8nejbkyclrov;

    alter table SMP_SUBRESOURCE_DEF 
       drop 
       foreign key FKbjqilcym6p3pptva2s4d1gw8o;

    alter table SMP_SUBRESOURCE_DEF_AUD 
       drop 
       foreign key FK1dd2l0ujtncg9u7hl3c4rte63;

    alter table SMP_USER_AUD 
       drop 
       foreign key FK2786r5minnkai3d22b191iiiq;

    drop table if exists SMP_ALERT;

    drop table if exists SMP_ALERT_AUD;

    drop table if exists SMP_ALERT_PROPERTY;

    drop table if exists SMP_ALERT_PROPERTY_AUD;

    drop table if exists SMP_CERTIFICATE;

    drop table if exists SMP_CERTIFICATE_AUD;

    drop table if exists SMP_CONFIGURATION;

    drop table if exists SMP_CONFIGURATION_AUD;

    drop table if exists SMP_CREDENTIAL;

    drop table if exists SMP_CREDENTIAL_AUD;

    drop table if exists SMP_DOCUMENT;

    drop table if exists SMP_DOCUMENT_AUD;

    drop table if exists SMP_DOCUMENT_PROPERTY;

    drop table if exists SMP_DOCUMENT_PROPERTY_AUD;

    drop table if exists SMP_DOCUMENT_VERSION;

    drop table if exists SMP_DOCUMENT_VERSION_AUD;

    drop table if exists SMP_DOCUMENT_VERSION_EVENT;

    drop table if exists SMP_DOMAIN;

    drop table if exists SMP_DOMAIN_AUD;

    drop table if exists SMP_DOMAIN_CONFIGURATION;

    drop table if exists SMP_DOMAIN_CONFIGURATION_AUD;

    drop table if exists SMP_DOMAIN_MEMBER;

    drop table if exists SMP_DOMAIN_MEMBER_AUD;

    drop table if exists SMP_DOMAIN_RESOURCE_DEF;

    drop table if exists SMP_DOMAIN_RESOURCE_DEF_AUD;

    drop table if exists SMP_EXTENSION;

    drop table if exists SMP_EXTENSION_AUD;

    drop table if exists SMP_GROUP;

    drop table if exists SMP_GROUP_AUD;

    drop table if exists SMP_GROUP_MEMBER;

    drop table if exists SMP_GROUP_MEMBER_AUD;

    drop table if exists SMP_RESOURCE;

    drop table if exists SMP_RESOURCE_AUD;

    drop table if exists SMP_RESOURCE_DEF;

    drop table if exists SMP_RESOURCE_DEF_AUD;

    drop table if exists SMP_RESOURCE_MEMBER;

    drop table if exists SMP_RESOURCE_MEMBER_AUD;

    drop table if exists SMP_REV_INFO;

    drop table if exists SMP_SUBRESOURCE;

    drop table if exists SMP_SUBRESOURCE_AUD;

    drop table if exists SMP_SUBRESOURCE_DEF;

    drop table if exists SMP_SUBRESOURCE_DEF_AUD;

    drop table if exists SMP_USER;

    drop table if exists SMP_USER_AUD;
