package io.github.luidmidev.storage.springframework.gridfs;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

@Configuration
@ConditionalOnClass({GridFsTemplate.class, GridFsOperations.class})
public class GridFSStorageAutoConfigurration {

    @Bean
    public GridFSStorage gridFSStorage(GridFsTemplate gridFsTemplate, GridFsOperations gridFsOperations) {
        return new GridFSStorage(gridFsTemplate, gridFsOperations);
    }
}