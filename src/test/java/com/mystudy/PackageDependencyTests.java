package com.mystudy;


import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packagesOf = MystudyApplication.class)
public class PackageDependencyTests {

    private static final String STUDY = "..modules.study..";
    private static final String EVENT = "..modules.event..";
    private static final String MEMBER = "..modules.member..";
    private static final String TAG = "..modules.tag..";
    private static final String ZONE = "..modules.zone..";

    @ArchTest
    ArchRule modulesPackageRule = classes().that().resideInAPackage("com.mystudy.modules..")
            .should().onlyBeAccessed().byClassesThat()
            .resideInAnyPackage("com.mystudy.modules..");

    @ArchTest
    ArchRule studyPackageRule = classes().that().resideInAPackage("..modules.study..")
            .should().onlyBeAccessed().byClassesThat()
            .resideInAnyPackage(STUDY, EVENT);

    @ArchTest
    ArchRule eventPackageRule = classes().that().resideInAPackage(EVENT)
            .should().accessClassesThat().resideInAnyPackage(STUDY, MEMBER, EVENT);

    @ArchTest
    ArchRule accountPackageRule = classes().that().resideInAPackage(MEMBER)
            .should().accessClassesThat().resideInAnyPackage(TAG, ZONE, MEMBER);

    @ArchTest
    ArchRule cycleCheck = slices().matching("com.mystudy.modules.(*)..")
            .should().beFreeOfCycles();
}
