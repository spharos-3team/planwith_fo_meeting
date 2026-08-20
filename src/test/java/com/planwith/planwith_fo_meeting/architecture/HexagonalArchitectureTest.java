package com.planwith.planwith_fo_meeting.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
		packages = "com.planwith.planwith_fo_meeting",
		importOptions = ImportOption.DoNotIncludeTests.class
)
class HexagonalArchitectureTest {

	@ArchTest
	static final ArchRule domainMustNotDependOnSpring = noClasses()
			.that().resideInAPackage("..domain..")
			.should().dependOnClassesThat().resideInAnyPackage(
					"org.springframework..",
					"jakarta.persistence..",
					"org.hibernate..",
					"org.springframework.data..",
					"org.apache.kafka..",
					"org.mongodb..",
					"redis.clients..",
					"io.lettuce.."
			);

	@ArchTest
	static final ArchRule domainMustNotDependOnAdapter = noClasses()
			.that().resideInAPackage("..domain..")
			.should().dependOnClassesThat().resideInAPackage("..adapter..");

	@ArchTest
	static final ArchRule applicationMustNotDependOnAdapter = noClasses()
			.that().resideInAPackage("..application..")
			.should().dependOnClassesThat().resideInAPackage("..adapter..");
}
