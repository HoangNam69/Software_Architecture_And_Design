package g5.kttkpm.productservice.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;
import org.springframework.data.mongodb.core.index.TextIndexDefinition.TextIndexDefinitionBuilder;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;


@Configuration
public class MongoDBTextIndexConfig {
    
    private final MongoTemplate mongoTemplate;
    private final MongoMappingContext mongoMappingContext;
    
    @Autowired
    public MongoDBTextIndexConfig(
        MongoTemplate mongoTemplate,
        MongoConverter mongoConverter) {
        this.mongoTemplate = mongoTemplate;
        this.mongoMappingContext = (MongoMappingContext) mongoConverter.getMappingContext();
    }
    
    @PostConstruct
    public void initIndices() {
        // Create text indices for better fuzzy searching
        ensureTextIndex();
        
        // Create all other indices defined by @Indexed annotations
        MongoPersistentEntityIndexResolver resolver =
            new MongoPersistentEntityIndexResolver(mongoMappingContext);
        
        mongoMappingContext.getPersistentEntities()
            .stream()
            .filter(entity -> entity.isAnnotationPresent(Document.class))
            .forEach(entity -> {
                resolver.resolveIndexFor(entity.getType())
                    .forEach(indexDefinition ->
                        mongoTemplate.indexOps(entity.getCollection())
                            .ensureIndex(indexDefinition));
            });
    }
    
    private void ensureTextIndex() {
        // Create text index for Product collection to enable fuzzy text search
        TextIndexDefinition textIndex = new TextIndexDefinitionBuilder()
            .onField("name", 3.0f)    // Higher weight for name
            .onField("description", 2.0f)
            .onField("sku", 2.0f)
            .onField("brand", 1.5f)
            .onField("additionalAttributes", 1.0f)
            .build();
        
        mongoTemplate.indexOps("products").ensureIndex(textIndex);
    }
}
