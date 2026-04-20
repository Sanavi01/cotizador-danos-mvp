"""
Plantilla para tests de adaptadores de persistencia con JUnit/Testcontainers.
Copia este contenido como referencia a `plataforma-danos-back/src/test/java/.../<Feature>RepositoryAdapterTest.java`.
"""

JAVA_TEMPLATE = r'''
@DataJpaTest
class FeatureRepositoryAdapterTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void save_persistsAggregate() {
        // GIVEN
        // WHEN
        // THEN
    }
}
'''
