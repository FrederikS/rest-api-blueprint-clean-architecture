package codes.fdk.blueprint.api.infrastructure.persistence.r2dbc;

import codes.fdk.blueprint.api.domain.model.Category;
import org.mapstruct.Mapper;

//TODO how to do with mapstruct (possible problem no access via reflection cuz of jpms)
@Mapper
interface CategoryEntityMapper {

    default CategoryEntity fromCategory(Category category) {
        return new CategoryEntity(
                category.id(),
                category.name(),
                category.slug(),
                category.parentId(),
                category.isVisible()
        );
    }

    default Category toCategory(CategoryEntity entity) {
        return new Category(
                entity.id(),
                entity.name(),
                entity.slug(),
                entity.parentId(),
                entity.isVisible()
        );
    }

}
