package custom.orm.test;

import custom.orm.db.utils.annotations.AutoGenerated;
import custom.orm.db.utils.annotations.Column;
import custom.orm.db.utils.annotations.Entity;
import custom.orm.db.utils.annotations.Id;

@Entity(tableName = "lab_types")
public class ExampleEntity {
    @Id
    @AutoGenerated
    int id;

    @Column(name = "name")
    String entityName;

    public ExampleEntity() {
    }

    public ExampleEntity(int id, String entityName) {
        this.id = id;
        this.entityName = entityName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    @Override
    public String toString() {
        return "ExampleEntity [getId()=" + getId() + ", getEntityName()=" + getEntityName() + "]";
    }
}
