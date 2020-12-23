package codes.fdk.blueprint.api.infrastructure.vertx.persistence.postgres

import codes.fdk.blueprint.api.domain.model.Category
import codes.fdk.blueprint.api.domain.model.CategoryId
import codes.fdk.blueprint.api.domain.spi.CategoryRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.reactor.flux
import kotlinx.coroutines.reactor.mono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

internal class CategoryEntityRepositoryAdapter(private val categoryEntityRepository: CategoryEntityRepository) : CategoryRepository {

    override fun save(category: Category): Mono<Category> {
        return mono { categoryEntityRepository.save(CategoryEntityMapper.toEntity(category)) }
            .map(CategoryEntityMapper::fromEntity)
    }

    override fun findAll(): Flux<Category> {
        return flux {
            categoryEntityRepository.findAll().collect { send(it) }
        }.map(CategoryEntityMapper::fromEntity)
    }

    override fun findById(id: CategoryId): Mono<Category> {
        return mono { categoryEntityRepository.findById(id) }
            .map(CategoryEntityMapper::fromEntity)
    }

    override fun findByParentId(parentId: CategoryId): Flux<Category> {
        return flux {
            categoryEntityRepository.findByParentId(parentId).collect { send(it) }
        }.map(CategoryEntityMapper::fromEntity)
    }

}