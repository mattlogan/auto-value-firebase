package me.mattlogan.auto.value.firebase;

import com.google.auto.service.AutoService;
import com.google.auto.value.extension.AutoValueExtension;
import com.google.common.collect.Lists;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

@AutoService(AutoValueExtension.class)
public class AutoValueFirebaseExtension extends AutoValueExtension {

  static final String AUTOVALUE_PREFIX = "AutoValue_";
  static final String FIREBASEVALUE = "FirebaseValue";

  static final ClassName STRING = ClassName.get("java.lang", "String");
  static final ClassName LIST = ClassName.get("java.util", "List");
  static final ClassName MAP = ClassName.get("java.util", "Map");
  static final ClassName ARRAY_LIST = ClassName.get("java.util", "ArrayList");
  static final ClassName HASH_MAP = ClassName.get("java.util", "HashMap");
  static final ClassName MAP_ENTRY = ClassName.get("java.util", "Map", "Entry");
  static final ClassName FIREBASE_VALUE_ANNOTATION =
    ClassName.get("me.mattlogan.auto.value.firebase.annotation", "FirebaseValue");
  static final ClassName IGNORE_EXTRA_PROPERTIES =
    ClassName.get("com.google.firebase.database", "IgnoreExtraProperties");
  static final ClassName THROW_ON_EXTRA_PROPERTIES =
    ClassName.get("com.google.firebase.database", "ThrowOnExtraProperties");
  static final ClassName EXCLUDE =
    ClassName.get("com.google.firebase.database", "Exclude");
  static final ClassName PROPERTY_NAME =
    ClassName.get("com.google.firebase.database", "PropertyName");

  @Override
  public boolean applicable(Context context) {
    for (AnnotationMirror annotation : context.autoValueClass().getAnnotationMirrors()) {
      if (FIREBASE_VALUE_ANNOTATION.equals(AnnotationSpec.get(annotation).type)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String generateClass(Context context, String className, String classToExtend, boolean isFinal) {
    String packageName = context.packageName();
    TypeElement autoValueTypeElement = context.autoValueClass();
    Map<String, ExecutableElement> properties = context.properties();
    LinkedHashMap<String, TypeName> types = convertPropertiesToTypes(properties);
    ClassName firebaseValueClassName = ClassName.get(context.packageName(), className, FIREBASEVALUE);

    Map<String, MethodSpec> collectionConverterMethods =
      generateCollectionConverterMethods(context.packageName(), types);

    TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className)
                                            .superclass(TypeVariableName.get(classToExtend))
                                            .addMethod(generateStandardAutoValueConstructor(types))
                                            .addMethods(collectionConverterMethods.values())
                                            .addMethod(generateAutoValueConstructorWithFirebaseValueParam(
                                              types,
                                              firebaseValueClassName,
                                              collectionConverterMethods))
                                            .addType(
                                              TypeSpec.classBuilder(FIREBASEVALUE)
                                                      .addModifiers(Modifier.STATIC, Modifier.FINAL)
                                                      .addAnnotations(generateFirebaseClassAnnotations(autoValueTypeElement))
                                                      .addFields(generateFirebaseValueFields(packageName, types))
                                                      .addMethod(generateEmptyFirebaseValueConstructor())
                                                      .addMethod(generateFirebaseValueConstructorWithAutoValueParam(
                                                        packageName, autoValueTypeElement, types))
                                                      .addMethods(generateFirebaseValueGetters(packageName, properties))
                                                      .build());

    return JavaFile.builder(context.packageName(), classBuilder.build()).build().toString();
  }

  static LinkedHashMap<String, TypeName> convertPropertiesToTypes(Map<String, ExecutableElement> properties) {
    LinkedHashMap<String, TypeName> types = new LinkedHashMap<>();
    for (Map.Entry<String, ExecutableElement> entry : properties.entrySet()) {
      types.put(entry.getKey(), TypeName.get(entry.getValue().getReturnType()));
    }
    return types;
  }

  static MethodSpec generateStandardAutoValueConstructor(Map<String, TypeName> properties) {
    List<ParameterSpec> params = Lists.newArrayList();
    for (Map.Entry<String, TypeName> entry : properties.entrySet()) {
      params.add(ParameterSpec.builder(entry.getValue(), entry.getKey()).build());
    }

    MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                                           .addParameters(params);

    StringBuilder superFormat = new StringBuilder("super(");
    for (int i = properties.size(); i > 0; i--) {
      superFormat.append("$N");
      if (i > 1) {
        superFormat.append(", ");
      }
    }
    superFormat.append(")");
    builder.addStatement(superFormat.toString(), properties.keySet().toArray());

    return builder.build();
  }

  static List<AnnotationSpec> generateFirebaseClassAnnotations(TypeElement type) {
    List<AnnotationSpec> annotations = new ArrayList<>();

    for (AnnotationMirror annotation : type.getAnnotationMirrors()) {
      AnnotationSpec annotationSpec = AnnotationSpec.get(annotation);
      TypeName annotationType = AnnotationSpec.get(annotation).type;

      if (IGNORE_EXTRA_PROPERTIES.equals(annotationType)) {
        annotations.add(annotationSpec);
      } else if (THROW_ON_EXTRA_PROPERTIES.equals(annotationType)) {
        annotations.add(annotationSpec);
      }
    }

    return annotations;
  }

  static List<FieldSpec> generateFirebaseValueFields(String packageName,
                                                     Map<String, TypeName> types) {
    List<FieldSpec> fields = new ArrayList<>();

    for (Map.Entry<String, TypeName> entry : types.entrySet()) {
      String fieldName = entry.getKey();
      TypeName originalType = entry.getValue();

      // This is important! This doesn't have to be here, but it's gotta be somewhere.
      checkIfTypeIsSupported(entry.getValue());

      if (typeIsPrimitive(originalType) || typeIsPrimitiveCollection(originalType)) {
        fields.add(FieldSpec.builder(originalType, fieldName, Modifier.PRIVATE).build());

      } else if (typeIsNonPrimitiveCollection(originalType)) {
        ParameterizedTypeName fullType = (ParameterizedTypeName) originalType;
        ClassName rawType = fullType.rawType;
        if (LIST.equals(rawType)) {
          ClassName typeParam = (ClassName) fullType.typeArguments.get(0);
          ClassName newTypeParam =
            ClassName.get(packageName, AUTOVALUE_PREFIX + typeParam.simpleName(), FIREBASEVALUE);
          ParameterizedTypeName newFullType =
            ParameterizedTypeName.get(rawType, newTypeParam);

          fields.add(FieldSpec.builder(newFullType, fieldName, Modifier.PRIVATE).build());

        } else if (MAP.equals(rawType)) {
          ClassName keyParam = (ClassName) fullType.typeArguments.get(0);
          ClassName valueParam = (ClassName) fullType.typeArguments.get(1);
          ClassName newTypeParam =
            ClassName.get(packageName, AUTOVALUE_PREFIX + valueParam.simpleName(), FIREBASEVALUE);
          ParameterizedTypeName newFullType =
            ParameterizedTypeName.get(rawType, keyParam, newTypeParam);

          fields.add(FieldSpec.builder(newFullType, fieldName, Modifier.PRIVATE).build());
        }
      } else {
        ClassName firebaseValueName =
          ClassName.get(packageName, AUTOVALUE_PREFIX + ((ClassName) originalType).simpleName(), FIREBASEVALUE);

        fields.add(FieldSpec.builder(firebaseValueName, fieldName, Modifier.PRIVATE).build());
      }
    }

    return fields;
  }

  static MethodSpec generateEmptyFirebaseValueConstructor() {
    return MethodSpec.constructorBuilder()
                     .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
                                                  .addMember("value", "\"unused\"")
                                                  .build())
                     .build();
  }

  static MethodSpec generateFirebaseValueConstructorWithAutoValueParam(String packageName,
                                                                       TypeElement autoValueTypeElement,
                                                                       Map<String, TypeName> types) {
    MethodSpec.Builder autoValueConstructorBuilder = MethodSpec.constructorBuilder();
    ClassName autoValueType = (ClassName) ClassName.get(autoValueTypeElement.asType());
    String autoValueConstructorParamName = firstLetterToLowerCase(autoValueType);
    autoValueConstructorBuilder.addParameter(
      ParameterSpec.builder(autoValueType, autoValueConstructorParamName).build());

    for (Map.Entry<String, TypeName> entry : types.entrySet()) {
      String fieldName = entry.getKey();
      TypeName originalType = entry.getValue();

      if (typeIsPrimitive(originalType) || typeIsPrimitiveCollection(originalType)) {
        autoValueConstructorBuilder.addCode("this.$L = $L.$L();\n",
          fieldName, autoValueConstructorParamName, fieldName);

      } else if (typeIsNonPrimitiveCollection(originalType)) {
        ParameterizedTypeName fullType = (ParameterizedTypeName) originalType;
        ClassName rawType = fullType.rawType;

        if (LIST.equals(rawType)) {
          ClassName typeParam = (ClassName) fullType.typeArguments.get(0);
          ClassName newTypeParam =
            ClassName.get(packageName, AUTOVALUE_PREFIX + typeParam.simpleName(), FIREBASEVALUE);

          // Convert the List in the constructor
          autoValueConstructorBuilder
            .beginControlFlow("if ($L.$L() != null)",
              autoValueConstructorParamName, fieldName)
            .addStatement("this.$L = new $T<>()", fieldName, ARRAY_LIST)
            .beginControlFlow("for ($T item : $L.$L())",
              typeParam, autoValueConstructorParamName, fieldName)
            .addStatement("$L.add(new $T(item))", fieldName, newTypeParam)
            .endControlFlow()
            .endControlFlow();

        } else if (MAP.equals(rawType)) {
          ClassName keyParam = (ClassName) fullType.typeArguments.get(0);
          ClassName valueParam = (ClassName) fullType.typeArguments.get(1);
          ClassName newTypeParam =
            ClassName.get(packageName, AUTOVALUE_PREFIX + valueParam.simpleName(), FIREBASEVALUE);

          // Convert the Map in the constructor
          autoValueConstructorBuilder
            .beginControlFlow("if ($L.$L() != null)",
              autoValueConstructorParamName, fieldName)
            .addStatement("this.$L = new $T<>()", fieldName, HASH_MAP)
            .beginControlFlow("for ($T<$T, $T> entry : $L.$L().entrySet())",
              MAP_ENTRY, keyParam, valueParam,
              autoValueConstructorParamName, fieldName)
            .addStatement("$L.put(entry.getKey(), new $T(entry.getValue()))",
              fieldName, newTypeParam)
            .endControlFlow()
            .endControlFlow();
        }
      } else {
        ClassName firebaseValueName =
          ClassName.get(packageName, AUTOVALUE_PREFIX + ((ClassName) originalType).simpleName(), FIREBASEVALUE);

        autoValueConstructorBuilder.addCode("this.$L = $L.$L() == null ? null : new $T($L.$L());\n",
          fieldName, autoValueConstructorParamName, fieldName,
          firebaseValueName, autoValueConstructorParamName, fieldName);
      }
    }

    return autoValueConstructorBuilder.build();
  }

  static List<MethodSpec> generateFirebaseValueGetters(String packageName,
                                                       Map<String, ExecutableElement> properties) {
    List<MethodSpec> getters = new ArrayList<>();

    for (Map.Entry<String, ExecutableElement> entry : properties.entrySet()) {
      String fieldName = entry.getKey();
      TypeName originalType = TypeName.get(entry.getValue().getReturnType());
      checkIfTypeIsSupported(originalType);

      MethodSpec.Builder methodBuilder =
        MethodSpec.methodBuilder(fieldNameToGetterName(fieldName))
                  .addModifiers(Modifier.PUBLIC)
                  .addCode("return " + fieldName + ";\n");

      methodBuilder.addAnnotations(generateFirebaseMethodAnnotations(entry.getValue()));

      if (typeIsPrimitive(originalType) || typeIsPrimitiveCollection(originalType)) {
        methodBuilder.returns(originalType);

      } else if (typeIsNonPrimitiveCollection(originalType)) {
        ParameterizedTypeName fullType = (ParameterizedTypeName) originalType;
        ClassName rawType = fullType.rawType;

        if (LIST.equals(rawType)) {
          ClassName typeParam = (ClassName) fullType.typeArguments.get(0);
          ClassName newTypeParam =
            ClassName.get(packageName, AUTOVALUE_PREFIX + typeParam.simpleName(), FIREBASEVALUE);
          ParameterizedTypeName newFullType =
            ParameterizedTypeName.get(rawType, newTypeParam);

          methodBuilder.returns(newFullType);

        } else if (MAP.equals(rawType)) {
          ClassName keyParam = (ClassName) fullType.typeArguments.get(0);
          ClassName valueParam = (ClassName) fullType.typeArguments.get(1);
          ClassName newTypeParam = ClassName.get(packageName,
            AUTOVALUE_PREFIX + valueParam.simpleName(),
            FIREBASEVALUE);
          ParameterizedTypeName newFullType =
            ParameterizedTypeName.get(rawType, keyParam, newTypeParam);

          methodBuilder.returns(newFullType);
        }

      } else {
        ClassName firebaseValueName =
          ClassName.get(packageName, AUTOVALUE_PREFIX + ((ClassName) originalType).simpleName(), FIREBASEVALUE);

        methodBuilder.returns(firebaseValueName);
      }

      getters.add(methodBuilder.build());
    }

    return getters;
  }

  static List<AnnotationSpec> generateFirebaseMethodAnnotations(ExecutableElement property) {
    List<AnnotationSpec> annotations = new ArrayList<>();

    for (AnnotationMirror annotation : property.getAnnotationMirrors()) {
      AnnotationSpec annotationSpec = AnnotationSpec.get(annotation);
      TypeName annotationType = AnnotationSpec.get(annotation).type;

      if (EXCLUDE.equals(annotationType)) {
        annotations.add(annotationSpec);
      } else if (PROPERTY_NAME.equals(annotationType)) {
        annotations.add(annotationSpec);
      }
    }

    return annotations;
  }

  static MethodSpec generateAutoValueConstructorWithFirebaseValueParam(LinkedHashMap<String, TypeName> types,
                                                                       ClassName firebaseValueType,
                                                                       Map<String, MethodSpec> converters) {
    String paramName = "firebaseValue";
    MethodSpec.Builder constructorBuilder =
      MethodSpec.constructorBuilder()
                .addParameter(firebaseValueType, paramName);
    constructorBuilder.addCode("super(\n");

    int i = 0;
    for (Map.Entry<String, TypeName> entry : types.entrySet()) {
      String fieldName = entry.getKey();
      TypeName type = entry.getValue();
      String getterName = fieldNameToGetterName(fieldName);

      if (typeIsPrimitive(type) || typeIsPrimitiveCollection(type)) {
        constructorBuilder.addCode(
          "  $L.$L()", paramName, getterName);

      } else if (typeIsNonPrimitiveCollection(type)) {
        constructorBuilder.addCode(
          "  $L($L.$L())", converters.get(fieldName).name, paramName, getterName);

      } else {
        constructorBuilder.addCode(
          "  $L.$L() == null ? null : new AutoValue_$T($L.$L())",
          paramName, getterName, type, paramName, getterName);

      }
      if (i < types.size() - 1) {
        constructorBuilder.addCode(",\n");
      }

      i++;
    }
    constructorBuilder.addCode(");\n");

    return constructorBuilder.build();
  }

  static Map<String, MethodSpec> generateCollectionConverterMethods(String packageName,
                                                                    LinkedHashMap<String, TypeName> types) {
    Map<String, MethodSpec> map = new HashMap<>();
    for (Map.Entry<String, TypeName> entry : types.entrySet()) {
      if (!typeIsNonPrimitiveCollection(entry.getValue())) {
        continue;
      }

      String fieldName = entry.getKey();
      ParameterizedTypeName returnType = (ParameterizedTypeName) entry.getValue();

      MethodSpec.Builder methodBuilder =
        MethodSpec.methodBuilder(fieldNameToConverterName(fieldName))
                  .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                  .returns(returnType);

      if (LIST.equals(returnType.rawType)) {
        ClassName outputParam = (ClassName) returnType.typeArguments.get(0);
        ClassName inputParam =
          ClassName.get(packageName, AUTOVALUE_PREFIX + outputParam.simpleName() + ".FirebaseValue");

        // Add method parameter
        methodBuilder.addParameter(
          ParameterizedTypeName.get(returnType.rawType, inputParam), fieldName);

        // Create method body
        methodBuilder.addCode("if ($L == null) return null;\n", fieldName);
        methodBuilder.addCode("$T list = new $T<>();\n", returnType, ARRAY_LIST);
        methodBuilder.beginControlFlow("for ($T item : $L)", inputParam, fieldName)
                     .addStatement("list.add(new $T(item))",
                       ClassName.get(packageName, AUTOVALUE_PREFIX + outputParam.simpleName()))
                     .endControlFlow();
        methodBuilder.addStatement("return list");

      } else if (MAP.equals(returnType.rawType)) {
        ClassName keyParam = (ClassName) returnType.typeArguments.get(0);
        ClassName outputParam = (ClassName) returnType.typeArguments.get(1);
        ClassName inputParam =
          ClassName.get(packageName, AUTOVALUE_PREFIX + outputParam.simpleName() + ".FirebaseValue");

        // Add method parameter
        methodBuilder.addParameter(
          ParameterizedTypeName.get(returnType.rawType, keyParam, inputParam), fieldName);

        // Create method body
        methodBuilder.addCode("if ($L == null) return null;\n", fieldName);
        methodBuilder.addCode("$T map = new $T<>();\n", returnType, HASH_MAP);
        methodBuilder.beginControlFlow("for ($T<$T, $T> entry : $L.entrySet())",
          MAP_ENTRY, keyParam, inputParam, fieldName)
                     .addStatement("map.put(entry.getKey(), new $T(entry.getValue()))",
                       ClassName.get(packageName, AUTOVALUE_PREFIX + outputParam.simpleName()))
                     .endControlFlow();
        methodBuilder.addStatement("return map");
      }

      map.put(fieldName, methodBuilder.build());
    }

    return map;
  }

  static boolean checkIfTypeIsSupported(TypeName type) {
    if (typeIsPrimitive(type)) {
      return true;

    } else if (type instanceof ParameterizedTypeName) {
      ParameterizedTypeName pType = (ParameterizedTypeName) type;

      if (LIST.equals(pType.rawType)) {
        if (pType.typeArguments.get(0) instanceof ParameterizedTypeName) {
          throw unsupportedType(type, "Parameterized types are not allowed as List type arguments");
        }

      } else if (MAP.equals(pType.rawType)) {
        if (!typeIsPrimitive(pType.typeArguments.get(0))) {
          throw unsupportedType(type, "Only primitives, boxed primitives, and Strings are allowed as Map keys");
        } else if (pType.typeArguments.get(1) instanceof ParameterizedTypeName) {
          throw unsupportedType(type, "Parameterized types are not allowed as Map values");
        }

      } else {
        throw unsupportedType(type, "List and Map are the only supported parameterized types");
      }
    }
    return true;
  }

  static boolean typeIsPrimitive(TypeName typeName) {
    return typeName.isPrimitive() || typeName.isBoxedPrimitive() || STRING.equals(typeName);
  }

  static boolean typeIsPrimitiveCollection(TypeName typeName) {
    if (typeName instanceof ParameterizedTypeName) {
      ParameterizedTypeName pTypeName = (ParameterizedTypeName) typeName;
      TypeName rawType = pTypeName.rawType;

      if (LIST.equals(rawType)) {
        return typeIsPrimitive(pTypeName.typeArguments.get(0));
      } else if (MAP.equals(rawType)) {
        return typeIsPrimitive(pTypeName.typeArguments.get(1));
      }
    }
    return false;
  }

  static boolean typeIsNonPrimitiveCollection(TypeName typeName) {
    if (typeName instanceof ParameterizedTypeName) {
      ParameterizedTypeName pTypeName = (ParameterizedTypeName) typeName;
      TypeName rawType = pTypeName.rawType;

      if (LIST.equals(rawType)) {
        return !typeIsPrimitive(pTypeName.typeArguments.get(0));
      } else if (MAP.equals(rawType)) {
        return !typeIsPrimitive(pTypeName.typeArguments.get(1));
      }
    }
    return false;
  }

  static String firstLetterToLowerCase(ClassName className) {
    return className.simpleName().substring(0, 1).toLowerCase() +
           className.simpleName().substring(1);
  }

  static String fieldNameToGetterName(String fieldName) {
    return "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
  }

  static String fieldNameToConverterName(String fieldName) {
    String fieldNameCapitalized = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    return "convertFirebase" + fieldNameCapitalized + "ToAutoValue" + fieldNameCapitalized;
  }

  static RuntimeException unsupportedType(TypeName type, String message) {
    return new RuntimeException("Type is not supported: " + type + "\n" + message);
  }
}
