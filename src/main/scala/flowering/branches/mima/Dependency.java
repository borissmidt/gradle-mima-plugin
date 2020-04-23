//package the.flowering.branches.mima;
//
//import org.gradle.api.file.FileCollection;
//import org.gradle.api.model.ObjectFactory;
//import org.gradle.api.provider.Property;
//
///**
// * Copyright (C) 18.04.20 - REstore NV
// */
//
//public class Dependency {
//    final private String artifactName;
//    final private Property<FileCollection> file;
//
//    public Dependency(String artifactName, ObjectFactory objectFactory) {
//        this.artifactName = artifactName;
//        this.file = objectFactory.property(FileCollection.class);
//        this.file.set(ResolveOldApi.oldApiProvider());
//    }
//
//    public String getArtifactName() {
//        return artifactName;
//    }
//
//    public Property<FileCollection> getFile() {
//        return file;
//    }
//}
