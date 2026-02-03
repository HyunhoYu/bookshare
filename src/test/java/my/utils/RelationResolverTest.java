package my.utils;

import my.common.vo.MyApplicationVO;
import my.domain.bankaccount.vo.BankAccountVO;
import my.domain.bookowner.vo.BookOwnerVO;
import my.domain.user.UserVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RelationResolverTest {

    private RelationResolver relationResolver;

    @BeforeEach
    void setUp() {
        relationResolver = new RelationResolver();
    }

    @Nested
    @DisplayName("resolve() 메서드")
    class ResolveTest {

        @Test
        @DisplayName("UserVO → BookOwnerVO → BankAccountVO 관계를 올바르게 파악한다")
        void resolveRelations_success() {
            // given
            UserVO userVO = new UserVO();
            BookOwnerVO bookOwnerVO = new BookOwnerVO();
            BankAccountVO bankAccountVO = new BankAccountVO();

            List<MyApplicationVO> entities = List.of(userVO, bookOwnerVO, bankAccountVO);

            // when
            Map<MyApplicationVO, MyApplicationVO> relations = relationResolver.resolve(entities);

            // then
            assertThat(relations).hasSize(2);
            assertThat(relations.get(bookOwnerVO)).isEqualTo(userVO);
            assertThat(relations.get(bankAccountVO)).isEqualTo(bookOwnerVO);
        }

        @Test
        @DisplayName("@Ref가 없는 엔티티(UserVO)는 relations의 key에 포함되지 않는다")
        void rootEntityNotInKeys() {
            // given
            UserVO userVO = new UserVO();
            BookOwnerVO bookOwnerVO = new BookOwnerVO();
            BankAccountVO bankAccountVO = new BankAccountVO();

            List<MyApplicationVO> entities = List.of(userVO, bookOwnerVO, bankAccountVO);

            // when
            Map<MyApplicationVO, MyApplicationVO> relations = relationResolver.resolve(entities);

            // then
            assertThat(relations.containsKey(userVO)).isFalse();
        }

        @Test
        @DisplayName("빈 리스트를 전달하면 빈 맵을 반환한다")
        void emptyList_returnsEmptyMap() {
            // given
            List<MyApplicationVO> entities = List.of();

            // when
            Map<MyApplicationVO, MyApplicationVO> relations = relationResolver.resolve(entities);

            // then
            assertThat(relations).isEmpty();
        }

        @Test
        @DisplayName("@Ref가 없는 엔티티만 있으면 빈 맵을 반환한다")
        void noRefAnnotation_returnsEmptyMap() {
            // given
            UserVO userVO = new UserVO();
            List<MyApplicationVO> entities = List.of(userVO);

            // when
            Map<MyApplicationVO, MyApplicationVO> relations = relationResolver.resolve(entities);

            // then
            assertThat(relations).isEmpty();
        }

        @Test
        @DisplayName("참조 대상이 리스트에 없으면 해당 관계의 value는 null이다")
        void referenceNotInList_valueIsNull() {
            // given
            BookOwnerVO bookOwnerVO = new BookOwnerVO();  // @Ref(reference = UserVO.class)
            List<MyApplicationVO> entities = List.of(bookOwnerVO);  // UserVO 없음

            // when
            Map<MyApplicationVO, MyApplicationVO> relations = relationResolver.resolve(entities);

            // then
            assertThat(relations).hasSize(1);
            assertThat(relations.get(bookOwnerVO)).isNull();
        }
    }

    @Nested
    @DisplayName("Thread Safety 테스트")
    class ThreadSafetyTest {

        @Test
        @DisplayName("여러 스레드에서 동시에 호출해도 각각 독립적인 결과를 반환한다")
        void concurrentCalls_independentResults() throws InterruptedException {
            // given
            int threadCount = 10;
            Thread[] threads = new Thread[threadCount];
            boolean[] results = new boolean[threadCount];

            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    UserVO userVO = new UserVO();
                    BookOwnerVO bookOwnerVO = new BookOwnerVO();
                    List<MyApplicationVO> entities = List.of(userVO, bookOwnerVO);

                    Map<MyApplicationVO, MyApplicationVO> relations = relationResolver.resolve(entities);

                    // 각 스레드의 결과가 독립적인지 확인
                    results[index] = relations.size() == 1
                            && relations.get(bookOwnerVO) == userVO;
                });
            }

            // when
            for (Thread thread : threads) {
                thread.start();
            }
            for (Thread thread : threads) {
                thread.join();
            }

            // then
            for (boolean result : results) {
                assertThat(result).isTrue();
            }
        }
    }
}
