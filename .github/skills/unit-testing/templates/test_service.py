"""
Plantilla para tests de casos de uso con JUnit 5 + Mockito.
Copia este contenido como referencia a `plataforma-danos-back/src/test/java/.../<Feature>UseCaseTest.java`.
"""

JAVA_TEMPLATE = r'''
@ExtendWith(MockitoExtension.class)
class FeatureUseCaseTest {

    @Mock
    private FeatureRepository repository;

    @InjectMocks
    private FeatureUseCase useCase;

    @Test
    void handle_returnsExpectedResult() {
        // GIVEN
        // WHEN
        // THEN
    }

    @Test
    void handle_throwsBusinessExceptionWhenRuleFails() {
        // GIVEN
        // WHEN
        // THEN
    }
}
'''
