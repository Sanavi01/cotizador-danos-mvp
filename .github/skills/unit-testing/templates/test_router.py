"""
Plantilla para tests de controllers Spring Boot con MockMvc.
Copia este contenido como referencia a `plataforma-danos-back/src/test/java/.../<Feature>ControllerTest.java`.
"""

JAVA_TEMPLATE = r'''
@WebMvcTest(FeatureController.class)
class FeatureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FeatureUseCase useCase;

    @Test
    void post_returns201() throws Exception {
        // GIVEN
        // WHEN
        // THEN
    }

    @Test
    void get_returns404WhenNotFound() throws Exception {
        // GIVEN
        // WHEN
        // THEN
    }
}
'''
