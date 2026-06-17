package com.wanted.backend.domain.community.infrastructure;

import com.wanted.backend.domain.community.infrastructure.persistence.CommentJpaEntity;
import com.wanted.backend.domain.community.infrastructure.persistence.SpringDataCommentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.jpa.database=H2",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@ActiveProfiles("test")
class CommunityRepositoryTest {

    @Autowired
    private SpringDataCommentRepository commentRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("게시글에 채택된 댓글이 존재하면 true를 반환한다")
    void existsByPostIdAndIsAcceptedTrue_returnTrue() {
        // given
        em.persist(new CommentJpaEntity(
                1L,
                1L,
                null,
                "채택된 댓글입니다.",
                true,
                false,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        ));
        em.flush();

        // when
        boolean result =
                commentRepository.existsByPostIdAndIsAcceptedTrue(1L);

        // then
        assertThat(result).isTrue();
    }
}