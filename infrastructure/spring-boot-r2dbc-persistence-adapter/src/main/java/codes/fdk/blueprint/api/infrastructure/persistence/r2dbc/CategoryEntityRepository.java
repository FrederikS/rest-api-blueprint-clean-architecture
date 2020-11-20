package codes.fdk.blueprint.api.infrastructure.persistence.r2dbc;

import codes.fdk.blueprint.api.domain.model.CategoryId;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

//TODO auditing, profiling, projections?
interface CategoryEntityRepository extends R2dbcRepository<CategoryEntity, CategoryId> {
    Flux<CategoryEntity> findByParentId(CategoryId parentId);
}
