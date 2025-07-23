package kr.hhplus.be.server;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.library.Architectures.LayeredArchitecture;

@AnalyzeClasses(
    packages = "kr.hhplus.be.server",
    importOptions = ImportOption.DoNotIncludeTests.class
)
public class LayeredArchitectureTest {
    private static final String CONTROLLER_LAYER = "Controller";
    private static final String APPLICATION_LAYER = "Application";
    private static final String DOMAIN_LAYER = "Domain";
    private static final String INFRA_LAYER = "Infrastructure";

    private static final String CONTROLLER_PACKAGE = "..controller..";
    private static final String APPLICATION_PACKAGE = "..application..";
    private static final String DOMAIN_PACKAGE = "..domain..";
    private static final String INFRA_PACKAGE = "..infra..";

    @ArchTest
    static final LayeredArchitecture 레이어검사_모든_계층의_의존흐름은_순방향_이어야한다 =
        layeredArchitecture()
            .consideringAllDependencies()
            .layer(CONTROLLER_LAYER).definedBy(CONTROLLER_PACKAGE)
            .layer(APPLICATION_LAYER).definedBy(APPLICATION_PACKAGE)
            .layer(DOMAIN_LAYER).definedBy(DOMAIN_PACKAGE)
            .layer(INFRA_LAYER).definedBy(INFRA_PACKAGE)
            .whereLayer(CONTROLLER_LAYER).mayNotBeAccessedByAnyLayer()
            .whereLayer(APPLICATION_LAYER).mayOnlyBeAccessedByLayers(CONTROLLER_LAYER)
            .whereLayer(DOMAIN_LAYER).mayOnlyBeAccessedByLayers(APPLICATION_LAYER, INFRA_LAYER)
            .whereLayer(INFRA_LAYER).mayNotBeAccessedByAnyLayer();
}
