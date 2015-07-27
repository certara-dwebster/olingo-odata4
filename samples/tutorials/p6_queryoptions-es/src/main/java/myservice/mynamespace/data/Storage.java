/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package myservice.mynamespace.data;

import java.util.ArrayList;
import java.util.List;

import myservice.mynamespace.service.DemoEdmProvider;
import myservice.mynamespace.util.Util;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.server.api.uri.UriParameter;

public class Storage {

  // represent our database
  private List<Entity> productList;
  private List<Entity> categoryList;

  public Storage() {

    productList = new ArrayList<Entity>();
    categoryList = new ArrayList<Entity>();

    // creating some sample data
    initProductSampleData();
    initCategorySampleData();
  }

  /* PUBLIC FACADE */

  public EntityCollection readEntitySetData(EdmEntitySet edmEntitySet) {
    EntityCollection entitySet = null;

    if (edmEntitySet.getName().equals(DemoEdmProvider.ES_PRODUCTS_NAME)) {
      entitySet = getProducts();
    } else if (edmEntitySet.getName().equals(DemoEdmProvider.ES_CATEGORIES_NAME)) {
      entitySet = getCategories();
    }

    return entitySet;
  }

  public Entity readEntityData(EdmEntitySet edmEntitySet, List<UriParameter> keyParams) {
    Entity entity = null;

    EdmEntityType edmEntityType = edmEntitySet.getEntityType();

    if (edmEntityType.getName().equals(DemoEdmProvider.ET_PRODUCT_NAME)) {
      entity = getProduct(edmEntityType, keyParams);
    } else if (edmEntityType.getName().equals(DemoEdmProvider.ET_CATEGORY_NAME)) {
      entity = getCategory(edmEntityType, keyParams);
    }

    return entity;
  }

  // Navigation

  public Entity getRelatedEntity(Entity entity, EdmEntityType relatedEntityType) {
    EntityCollection collection = getRelatedEntityCollection(entity, relatedEntityType);
    if (collection.getEntities().isEmpty()) {
      return null;
    }
    return collection.getEntities().get(0);
  }

  public Entity getRelatedEntity(Entity entity, EdmEntityType relatedEntityType, List<UriParameter> keyPredicates) {

    EntityCollection relatedEntities = getRelatedEntityCollection(entity, relatedEntityType);
    return Util.findEntity(relatedEntityType, relatedEntities, keyPredicates);
  }

  public EntityCollection getRelatedEntityCollection(Entity sourceEntity, EdmEntityType targetEntityType) {
    EntityCollection navigationTargetEntityCollection = new EntityCollection();

    FullQualifiedName relatedEntityFqn = targetEntityType.getFullQualifiedName();
    String sourceEntityFqn = sourceEntity.getType();

    if (sourceEntityFqn.equals(DemoEdmProvider.ET_PRODUCT_FQN.getFullQualifiedNameAsString())
        && relatedEntityFqn.equals(DemoEdmProvider.ET_CATEGORY_FQN)) {
      // relation Products->Category (result all categories)
      int productID = (Integer) sourceEntity.getProperty("ID").getValue();
      if (productID == 1 || productID == 2) {
        navigationTargetEntityCollection.getEntities().add(categoryList.get(0));
      } else if (productID == 3 || productID == 4) {
        navigationTargetEntityCollection.getEntities().add(categoryList.get(1));
      } else if (productID == 5 || productID == 6) {
        navigationTargetEntityCollection.getEntities().add(categoryList.get(2));
      }
    } else if (sourceEntityFqn.equals(DemoEdmProvider.ET_CATEGORY_FQN.getFullQualifiedNameAsString())
        && relatedEntityFqn.equals(DemoEdmProvider.ET_PRODUCT_FQN)) {
      // relation Category->Products (result all products)
      int categoryID = (Integer) sourceEntity.getProperty("ID").getValue();
      if (categoryID == 1) {
        // the first 2 products are notebooks
        navigationTargetEntityCollection.getEntities().addAll(productList.subList(0, 2));
      } else if (categoryID == 2) {
        // the next 2 products are organizers
        navigationTargetEntityCollection.getEntities().addAll(productList.subList(2, 4));
      } else if (categoryID == 3) {
        // the first 2 products are monitors
        navigationTargetEntityCollection.getEntities().addAll(productList.subList(4, 6));
      }
    }

    if (navigationTargetEntityCollection.getEntities().isEmpty()) {
      return null;
    }

    return navigationTargetEntityCollection;
  }

  /* INTERNAL */

  private EntityCollection getProducts() {
    EntityCollection retEntitySet = new EntityCollection();

    for (Entity productEntity : this.productList) {
      retEntitySet.getEntities().add(productEntity);
    }

    return retEntitySet;
  }

  private Entity getProduct(EdmEntityType edmEntityType, List<UriParameter> keyParams) {

    // the list of entities at runtime
    EntityCollection entityCollection = getProducts();

    /* generic approach to find the requested entity */
    return Util.findEntity(edmEntityType, entityCollection, keyParams);
  }

  private EntityCollection getCategories() {
    EntityCollection entitySet = new EntityCollection();

    for (Entity categoryEntity : this.categoryList) {
      entitySet.getEntities().add(categoryEntity);
    }

    return entitySet;
  }

  private Entity getCategory(EdmEntityType edmEntityType, List<UriParameter> keyParams) {

    // the list of entities at runtime
    EntityCollection entitySet = getCategories();

    /* generic approach to find the requested entity */
    return Util.findEntity(edmEntityType, entitySet, keyParams);
  }

  /* HELPER */

  private void initProductSampleData() {

    Entity entity = new Entity();

    entity.addProperty(new Property(null, "ID", ValueType.PRIMITIVE, 1));
    entity.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, "Notebook Basic 15"));
    entity.addProperty(new Property(null, "Description", ValueType.PRIMITIVE,
        "Notebook Basic, 1.7GHz - 15 XGA - 1024MB DDR2 SDRAM - 40GB"));
    entity.setType(DemoEdmProvider.ET_PRODUCT_FQN.getFullQualifiedNameAsString());
    productList.add(entity);

    entity = new Entity();
    entity.addProperty(new Property(null, "ID", ValueType.PRIMITIVE, 2));
    entity.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, "Notebook Professional 17"));
    entity.addProperty(new Property(null, "Description", ValueType.PRIMITIVE,
        "Notebook Professional, 2.8GHz - 15 XGA - 8GB DDR3 RAM - 500GB"));
    entity.setType(DemoEdmProvider.ET_PRODUCT_FQN.getFullQualifiedNameAsString());
    productList.add(entity);

    entity = new Entity();
    entity.addProperty(new Property(null, "ID", ValueType.PRIMITIVE, 3));
    entity.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, "1UMTS PDA"));
    entity.addProperty(new Property(null, "Description", ValueType.PRIMITIVE,
        "Ultrafast 3G UMTS/HSDPA Pocket PC, supports GSM network"));
    entity.setType(DemoEdmProvider.ET_PRODUCT_FQN.getFullQualifiedNameAsString());
    productList.add(entity);

    entity = new Entity();
    entity.addProperty(new Property(null, "ID", ValueType.PRIMITIVE, 4));
    entity.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, "Comfort Easy"));
    entity.addProperty(new Property(null, "Description", ValueType.PRIMITIVE,
        "32 GB Digital Assitant with high-resolution color screen"));
    entity.setType(DemoEdmProvider.ET_PRODUCT_FQN.getFullQualifiedNameAsString());
    productList.add(entity);

    entity = new Entity();
    entity.addProperty(new Property(null, "ID", ValueType.PRIMITIVE, 5));
    entity.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, "Ergo Screen"));
    entity.addProperty(new Property(null, "Description", ValueType.PRIMITIVE,
        "19 Optimum Resolution 1024 x 768 @ 85Hz, resolution 1280 x 960"));
    entity.setType(DemoEdmProvider.ET_PRODUCT_FQN.getFullQualifiedNameAsString());
    productList.add(entity);

    entity = new Entity();
    entity.addProperty(new Property(null, "ID", ValueType.PRIMITIVE, 6));
    entity.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, "Flat Basic"));
    entity.addProperty(new Property(null, "Description", ValueType.PRIMITIVE,
        "Optimum Hi-Resolution max. 1600 x 1200 @ 85Hz, Dot Pitch: 0.24mm"));
    entity.setType(DemoEdmProvider.ET_PRODUCT_FQN.getFullQualifiedNameAsString());
    productList.add(entity);
  }

  private void initCategorySampleData() {

    Entity entity = new Entity();

    entity.addProperty(new Property(null, "ID", ValueType.PRIMITIVE, 1));
    entity.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, "Notebooks"));
    entity.setType(DemoEdmProvider.ET_CATEGORY_FQN.getFullQualifiedNameAsString());
    categoryList.add(entity);

    entity = new Entity();
    entity.addProperty(new Property(null, "ID", ValueType.PRIMITIVE, 2));
    entity.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, "Organizers"));
    entity.setType(DemoEdmProvider.ET_CATEGORY_FQN.getFullQualifiedNameAsString());
    categoryList.add(entity);

    entity = new Entity();
    entity.addProperty(new Property(null, "ID", ValueType.PRIMITIVE, 3));
    entity.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, "Monitors"));
    entity.setType(DemoEdmProvider.ET_CATEGORY_FQN.getFullQualifiedNameAsString());
    categoryList.add(entity);
  }

}
