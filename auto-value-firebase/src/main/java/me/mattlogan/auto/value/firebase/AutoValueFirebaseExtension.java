package me.mattlogan.auto.value.firebase;

import com.google.auto.service.AutoService;
import com.google.auto.value.extension.AutoValueExtension;
import com.google.common.collect.Lists;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import me.mattlogan.auto.value.firebase.adapter.FirebaseAdapter;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

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
  private static Types typeUtils;

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
  public String generateClass(Context context, String classNameString, String classToExtend, boolean isFinal) {
    String packageName = context.packageName();
    typeUtils = context.processingEnvironment().getTypeUtils();
    TypeElement autoValueTypeElement = context.autoValueClass();
    Map<String, ExecutableElement> properties = context.properties();
    LinkedHashMap<String, TypeName> types = convertPropertiesToTypes(properties);
    ClassName className = ClassName.get(packageName, classNameString);

    TypeSpec firebaseValue = TypeSpec.classBuilder(FIREBASEVALUE)
                                     .addModifiers(STATIC, FINAL)
                                     .addAnnotations(generateFirebaseValueClassAnnotations(autoValueTypeElement))
                                     .addFields(generateFirebaseValueFields(packageName, types))
                                     .addFields(generateAdapterFields(types))
                                     .addMethod(generateEmptyFirebaseValueConstructor())
                                     .addMethod(generateFirebaseValueConstructorWithAutoValueParam(
                                       packageName, autoValueTypeElement, types))
                                     .addMethod(generateFirebaseValueToAutoValueMethod(
                                       packageName, className, types))
                                     .addMethods(generateFirebaseValueGetters(packageName, properties))
                                     .build();

    TypeSpec generatedClass = TypeSpec.classBuilder(className)
                                      .superclass(TypeVariableName.get(classToExtend))
                                      .addMethod(generateStandardAutoValueConstructor(types))
                                      .addType(firebaseValue)
                                      .addModifiers(isFinal ? FINAL : ABSTRACT)
                                      .build();

    return JavaFile.builder(packageName, generatedClass).build().toString();
  }

  static Set<FieldSpec> generateAdapterFields(Map<String, TypeName> types) {
    Set<FieldSpec> fieldSpecs = new LinkedHashSet<>();

    for (String key : types.keySet()) {
      TypeName typeName = types.get(key);
      if (typeHasAdapter(typeName)) {
        AnnotationSpec typeAdapterSpec = getTypeAdapterSpec(typeName);
        ClassName typeAdapterClassName = ClassName.bestGuess(typeAdapterSpec.members
          .get("value")
          .get(0)
          .toString());
        fieldSpecs.add(FieldSpec.builder(typeAdapterClassName,
          firstLetterToLowerCase(typeAdapterClassName), PRIVATE, FINAL)
                                .initializer("new $T()", typeAdapterClassName).build());
      }
    }

    return fieldSpecs;
  }
  static LinkedHashMap<String, TypeName> convertPropertiesToTypes(Map<String, ExecutableElement> properties) {
    LinkedHashMap<String, TypeName> types = new LinkedHashMap<>();
    for (Map.Entry<String, ExecutableElement> entry : properties.entrySet()) {
      TypeName returnTypeName;
      ExecutableElement element = entry.getValue();
      returnTypeName = getTypeNameForElement(element);
      types.put(entry.getKey(), returnTypeName);
    }
    return types;
  }

  static MethodSpec generateStandardAutoValueConstructor(Map<String, TypeName> properties) {
    List<ParameterSpec> params = Lists.newArrayList();
    for (Map.Entry<String, TypeName> entry : properties.entrySet()) {
      params.add(ParameterSpec.builder(entry.getValue().withoutAnnotations(), entry.getKey()).build());
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

  static List<AnnotationSpec> generateFirebaseValueClassAnnotations(TypeElement type) {
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

      if (typeHasAdapter(originalType)) {
        ClassName output = getTypeAdapterOutputType(originalType);
        fields.add(FieldSpec.builder(output, fieldName, PRIVATE).build());
      } else if (typeIsPrimitive(originalType) || typeIsPrimitiveCollection(originalType)) {
        fields.add(FieldSpec.builder(originalType, fieldName, PRIVATE).build());

      } else if (typeIsNonPrimitiveCollection(originalType)) {
        ParameterizedTypeName fullType = (ParameterizedTypeName) originalType;
        ClassName rawType = fullType.rawType;
        if (LIST.equals(rawType)) {
          ClassName typeParam = (ClassName) fullType.typeArguments.get(0);
          ClassName newTypeParam =
            ClassName.get(packageName, AUTOVALUE_PREFIX + typeParam.simpleName(), FIREBASEVALUE);
          ParameterizedTypeName newFullType =
            ParameterizedTypeName.get(rawType, newTypeParam);

          fields.add(FieldSpec.builder(newFullType, fieldName, PRIVATE).build());

        } else if (MAP.equals(rawType)) {
          ClassName keyParam = (ClassName) fullType.typeArguments.get(0);
          ClassName valueParam = (ClassName) fullType.typeArguments.get(1);
          ClassName newTypeParam =
            ClassName.get(packageName, AUTOVALUE_PREFIX + valueParam.simpleName(), FIREBASEVALUE);
          ParameterizedTypeName newFullType =
            ParameterizedTypeName.get(rawType, keyParam, newTypeParam);

          fields.add(FieldSpec.builder(newFullType, fieldName, PRIVATE).build());
        }
      } else {
        ClassName firebaseValueName =
          ClassName.get(packageName, AUTOVALUE_PREFIX + ((ClassName) originalType).simpleName(), FIREBASEVALUE);

        fields.add(FieldSpec.builder(firebaseValueName, fieldName, PRIVATE).build());
      }
    }

    return fields;
  }

  static MethodSpec generateEmptyFirebaseValueConstructor() {
    return MethodSpec.constructorBuilder()
                     .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
        .addMember("value", "\"unused\"")
        .build()).build();
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

      if (typeHasAdapter(originalType)) {
        AnnotationSpec typeAdapterSpec = getTypeAdapterSpec(originalType);
        ClassName adapterInstance = ClassName.bestGuess(typeAdapterSpec.members
          .get("value")
          .get(0)
          .toString());
        autoValueConstructorBuilder.addCode("this.$L = $L.$L() == null ? null " +
          ": $L.toFirebaseValue($L.$L());\n",
          fieldName, autoValueConstructorParamName, fieldName,
          firstLetterToLowerCase(adapterInstance), autoValueConstructorParamName, fieldName);
      } else if (typeIsPrimitive(originalType) || typeIsPrimitiveCollection(originalType)) {
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
            .addStatement("this.$L.add(new $T(item))", fieldName, newTypeParam)
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
            .addStatement("this.$L.put(entry.getKey(), new $T(entry.getValue()))",
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
                  .addModifiers(PUBLIC)
                  .addCode("return " + fieldName + ";\n");

      methodBuilder.addAnnotations(generateFirebaseValueMethodAnnotations(entry.getValue()));

      if (typeHasAdapter(entry.getValue())) {
        TypeName typeNameForElement = getTypeNameForElement(entry.getValue());
        methodBuilder.returns(getTypeAdapterOutputType(typeNameForElement));
      } else if (typeIsPrimitive(originalType) || typeIsPrimitiveCollection(originalType)) {
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

  static List<AnnotationSpec> generateFirebaseValueMethodAnnotations(ExecutableElement property) {
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

  static MethodSpec generateFirebaseValueToAutoValueMethod(String packageName,
                                                           ClassName autoValueClassName,
                                                           LinkedHashMap<String, TypeName> types) {
    ClassName finalAutoValueClassName = stripDollarSignsFromClassName(autoValueClassName);
    MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("toAutoValue")
                                                 .addAnnotation(EXCLUDE)
                                                 .returns(finalAutoValueClassName);

    for (Map.Entry<String, TypeName> entry : types.entrySet()) {
      String fieldName = entry.getKey();
      boolean hasTypeAdapter = typeHasAdapter(entry.getValue());
      TypeName type = entry.getValue().withoutAnnotations();

      if (hasTypeAdapter) {
        AnnotationSpec typeAdapterSpec = getTypeAdapterSpec(entry.getValue());
        Map<String, List<CodeBlock>> annotationmemebers = typeAdapterSpec.members;
        ClassName adapterInstance = ClassName.bestGuess(annotationmemebers.get("value").get(0)
          .toString());
        methodBuilder.addStatement("$T $L = this.$L == null ? null " +
          ": $L.fromFirebaseValue(this.$L)",
          type, fieldName, fieldName, firstLetterToLowerCase(adapterInstance), fieldName);

      } else if (typeIsPrimitive(type) || typeIsPrimitiveCollection(type)) {
        methodBuilder.addStatement("$T $L = this.$L", type, fieldName, fieldName);

      } else if (typeIsNonPrimitiveCollection(type)) {
        ParameterizedTypeName pType = (ParameterizedTypeName) type;

        if (LIST.equals(pType.rawType)) {
          ClassName outputParam = (ClassName) pType.typeArguments.get(0);
          ClassName inputParam =
            ClassName.get(packageName, AUTOVALUE_PREFIX + outputParam.simpleName() + "." + FIREBASEVALUE);

          methodBuilder.addStatement("$T $L = null", type, fieldName)
                       .beginControlFlow("if (this.$L != null)", fieldName)
                       .addStatement("$L = new $T<>()", fieldName, ARRAY_LIST)
                       .beginControlFlow("for ($T item : this.$L)", inputParam, fieldName)
                       .addStatement("$L.add(item.toAutoValue())", fieldName)
                       .endControlFlow()
                       .endControlFlow();

        } else if (MAP.equals(pType.rawType)) {
          ClassName keyParam = (ClassName) pType.typeArguments.get(0);
          ClassName outputParam = (ClassName) pType.typeArguments.get(1);
          ClassName inputParam =
            ClassName.get(packageName, AUTOVALUE_PREFIX + outputParam.simpleName() + "." + FIREBASEVALUE);

          methodBuilder.addStatement("$T $L = null", type, fieldName)
                       .beginControlFlow("if (this.$L != null)", fieldName)
                       .addStatement("$L = new $T<>()", fieldName, HASH_MAP)
                       .beginControlFlow("for ($T<$T, $T> entry : this.$L.entrySet())",
                         MAP_ENTRY, keyParam, inputParam, fieldName)
                       .addStatement("$L.put(entry.getKey(), entry.getValue().toAutoValue())",
                         fieldName)
                       .endControlFlow()
                       .endControlFlow();
        }

      } else {
        methodBuilder.addStatement("$T $L = this.$L == null ? null : this.$L.toAutoValue()",
          type, fieldName, fieldName, fieldName);
      }
    }

    methodBuilder.addCode("return new $T(", finalAutoValueClassName);
    StringBuilder constructorArgsFormat = new StringBuilder();
    for (int i = types.size(); i > 0; i--) {
      constructorArgsFormat.append("$N");
      if (i > 1) {
        constructorArgsFormat.append(", ");
      }
    }
    constructorArgsFormat.append(");\n");
    methodBuilder.addCode(constructorArgsFormat.toString(), types.keySet().toArray());

    return methodBuilder.build();
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

  static boolean typeHasAdapter(TypeName typeName) {
    boolean hasAdapter = false;
    for (AnnotationSpec annotation : typeName.annotations) {
      if (annotation.type.equals(AnnotationSpec.builder(FirebaseAdapter.class).build().type)) {
        hasAdapter = true;
      }
    }
    return hasAdapter;
  }

  static boolean typeHasAdapter(Element element) {
    return element.getAnnotation(FirebaseAdapter.class) != null;
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

  static RuntimeException unsupportedType(TypeName type, String message) {
    return new RuntimeException("Type is not supported: " + type + "\n" + message);
  }

  // Get the "final" class in the AutoValue chain, if multiple extensions
  static ClassName stripDollarSignsFromClassName(ClassName className) {
    String simpleName = className.simpleName();
    if (!simpleName.startsWith("$")) {
      return className;
    }

    int numDollarSigns = 0;
    char dollarSign = "$".charAt(0);
    for (int i = 0; i < simpleName.length(); i++) {
      if (dollarSign == simpleName.charAt(i)) {
        numDollarSigns++;
      }
    }

    String strippedSimpleName = simpleName.substring(numDollarSigns);
    return ClassName.get(className.packageName(), strippedSimpleName);
  }

  static TypeMirror getTypeAdapterClass(FirebaseAdapter firebaseAdapter) {
    try {
      firebaseAdapter.value();
    } catch (MirroredTypeException e) {
      return e.getTypeMirror();
    }
    return null;
  }

  static AnnotationSpec getTypeAdapterSpec(TypeName originalType) {
    AnnotationSpec firebaseAdapter = AnnotationSpec.builder(FirebaseAdapter.class).build();
    AnnotationSpec typeAdapterSpec = null;
    for (AnnotationSpec annotation : originalType.annotations) {
      if (annotation.type.withoutAnnotations().equals(firebaseAdapter.type)) {
        typeAdapterSpec= annotation;
        break;
      }
    }
    return typeAdapterSpec;
  }

  private static ClassName getTypeAdapterOutputType(TypeName originalType) {
    Map<String, List<CodeBlock>> members = originalType.annotations.get(0).members;
    return ClassName.bestGuess(members.get("output").get(0).toString());
  }

  private static TypeName getTypeNameForElement(ExecutableElement element) {
    TypeName returnTypeName;
    if (typeHasAdapter(element)) {
      TypeMirror typeAdapterClass = getTypeAdapterClass(element
        .getAnnotation(FirebaseAdapter.class));

      TypeElement typeAdapterTypeElement = (TypeElement) typeUtils.asElement(typeAdapterClass);
      List<? extends TypeMirror> interfaces = typeAdapterTypeElement.getInterfaces();
      DeclaredType typeAdapterDeclaredType = (DeclaredType) interfaces.get(0);
      List<? extends TypeMirror> typeArguments = typeAdapterDeclaredType.getTypeArguments();

      returnTypeName = TypeName.get(element.getReturnType());
      returnTypeName = returnTypeName.annotated(AnnotationSpec.builder(FirebaseAdapter.class)
        .addMember("value", "$T", TypeName.get(typeAdapterClass))
        .addMember("input", "$T", TypeName.get(typeArguments.get(0)))
        .addMember("output", "$T", TypeName.get(typeArguments.get(1)))
        .build());
    } else {
      returnTypeName = TypeName.get(element.getReturnType());
    }
    return returnTypeName;
  }

}
