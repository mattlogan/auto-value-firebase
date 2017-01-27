package me.mattlogan.auto.value.firebase;

import com.google.auto.value.processor.AutoValueProcessor;
import com.google.testing.compile.JavaFileObjects;
import java.util.Arrays;
import javax.tools.JavaFileObject;
import org.junit.Test;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;

public class AutoValueFirebaseExtensionTest {

  private static final JavaFileObject INGREDIENT = JavaFileObjects.forSourceString("test.Ingredient",
    "package test;\n"
    + "\n"
    + "import com.google.auto.value.AutoValue;\n"
    + "import me.mattlogan.auto.value.firebase.annotation.FirebaseValue;\n"
    + "\n"
    + "@AutoValue @FirebaseValue\n"
    + "public abstract class Ingredient {\n"
    + "  public abstract int spiciness();\n"
    + "}\n");

  private static final JavaFileObject REVIEW = JavaFileObjects.forSourceString("test.Review",
    "package test;\n"
    + "\n"
    + "import com.google.auto.value.AutoValue;\n"
    + "import me.mattlogan.auto.value.firebase.annotation.FirebaseValue;\n"
    + "\n"
    + "@AutoValue @FirebaseValue\n"
    + "public abstract class Review {\n"
    + "  public abstract String description();\n"
    + "  public abstract int rating();\n"
    + "}\n");

  private static final JavaFileObject EXCLUDE = JavaFileObjects.forSourceString("com.google.firebase.database.Exclude",
    "package com.google.firebase.database;\n"
    + "\n"
    + "import java.lang.annotation.ElementType;\n"
    + "import java.lang.annotation.Retention;\n"
    + "import java.lang.annotation.RetentionPolicy;\n"
    + "import java.lang.annotation.Target;\n"
    + "\n"
    + "@Retention(RetentionPolicy.RUNTIME)\n"
    + "@Target({ElementType.METHOD, ElementType.FIELD})\n"
    + "public @interface Exclude {\n"
    + "}\n");

  private static final JavaFileObject PROPERTY_NAME =
    JavaFileObjects.forSourceString("com.google.firebase.database.PropertyName",
      "package com.google.firebase.database;\n"
      + "\n"
      + "import java.lang.annotation.ElementType;\n"
      + "import java.lang.annotation.Retention;\n"
      + "import java.lang.annotation.RetentionPolicy;\n"
      + "import java.lang.annotation.Target;\n"
      + "\n"
      + "@Retention(RetentionPolicy.RUNTIME)\n"
      + "@Target({ElementType.METHOD, ElementType.FIELD})\n"
      + "public @interface PropertyName {\n"
      + "  String value();\n"
      + "}\n");

  private static final JavaFileObject IGNORE_EXTRA_PROPERTIES =
    JavaFileObjects.forSourceString("com.google.firebase.database.IgnoreExtraProperties",
      "package com.google.firebase.database;\n"
      + "\n"
      + "import java.lang.annotation.ElementType;\n"
      + "import java.lang.annotation.Retention;\n"
      + "import java.lang.annotation.RetentionPolicy;\n"
      + "import java.lang.annotation.Target;\n"
      + "\n"
      + "@Retention(RetentionPolicy.RUNTIME)\n"
      + "@Target({ElementType.TYPE})\n"
      + "public @interface IgnoreExtraProperties {\n"
      + "}\n");

  private static final JavaFileObject THROW_ON_EXTRA_PROPERTIES =
    JavaFileObjects.forSourceString("com.google.firebase.database.ThrowOnExtraProperties",
      "package com.google.firebase.database;\n"
      + "\n"
      + "import java.lang.annotation.ElementType;\n"
      + "import java.lang.annotation.Retention;\n"
      + "import java.lang.annotation.RetentionPolicy;\n"
      + "import java.lang.annotation.Target;\n"
      + "\n"
      + "@Retention(RetentionPolicy.RUNTIME)\n"
      + "@Target({ElementType.TYPE})\n"
      + "public @interface ThrowOnExtraProperties {\n"
      + "}\n");

  @Test
  public void primitive() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Ingredient",
      "package test;\n"
      + "\n"
      + "import com.google.auto.value.AutoValue;\n"
      + "import me.mattlogan.auto.value.firebase.annotation.FirebaseValue;\n"
      + "\n"
      + "@AutoValue @FirebaseValue\n"
      + "public abstract class Ingredient {\n"
      + "  public abstract int spiciness();\n"
      + "}\n");

    JavaFileObject expected = JavaFileObjects.forSourceString("test.AutoValue_Ingredient",
      "package test;\n"
      + "\n"
      + "import com.google.firebase.database.Exclude;\n"
      + "import java.lang.SuppressWarnings;\n"
      + "\n"
      + "final class AutoValue_Ingredient extends $AutoValue_Ingredient {\n"
      + "  AutoValue_Ingredient(int spiciness) {\n"
      + "    super(spiciness);\n"
      + "  }\n"
      + "\n"
      + "  static final class FirebaseValue {\n"
      + "    private int spiciness;\n"
      + "    @SuppressWarnings(\"unused\")\n"
      + "    FirebaseValue() {\n"
      + "    }\n"
      + "    FirebaseValue(Ingredient ingredient) {\n"
      + "      this.spiciness = ingredient.spiciness();\n"
      + "    }\n"
      + "    @Exclude\n"
      + "    AutoValue_Ingredient toAutoValue() {\n"
      + "      int spiciness = this.spiciness;\n"
      + "      return new AutoValue_Ingredient(spiciness);\n"
      + "    }\n"
      + "    public int getSpiciness() {\n"
      + "      return spiciness;\n"
      + "    }\n"
      + "  }\n"
      + "}\n");

    assertAbout(javaSources())
      .that(Arrays.asList(EXCLUDE, source))
      .processedWith(new AutoValueProcessor())
      .compilesWithoutError()
      .and()
      .generatesSources(expected);
  }

  @Test
  public void boxedPrimitive() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Ingredient",
      "package test;\n"
      + "\n"
      + "import com.google.auto.value.AutoValue;\n"
      + "import me.mattlogan.auto.value.firebase.annotation.FirebaseValue;\n"
      + "\n"
      + "@AutoValue @FirebaseValue\n"
      + "public abstract class Ingredient {\n"
      + "  public abstract Integer spiciness();\n"
      + "}\n");

    JavaFileObject expected = JavaFileObjects.forSourceString("test.AutoValue_Ingredient",
      "package test;\n"
      + "\n"
      + "import com.google.firebase.database.Exclude;\n"
      + "import java.lang.Integer;\n"
      + "import java.lang.SuppressWarnings;\n"
      + "\n"
      + "final class AutoValue_Ingredient extends $AutoValue_Ingredient {\n"
      + "  AutoValue_Ingredient(Integer spiciness) {\n"
      + "    super(spiciness);\n"
      + "  }\n"
      + "\n"
      + "  static final class FirebaseValue {\n"
      + "    private Integer spiciness;\n"
      + "    @SuppressWarnings(\"unused\")\n"
      + "    FirebaseValue() {\n"
      + "    }\n"
      + "    FirebaseValue(Ingredient ingredient) {\n"
      + "      this.spiciness = ingredient.spiciness();\n"
      + "    }\n"
      + "    @Exclude\n"
      + "    AutoValue_Ingredient toAutoValue() {\n"
      + "      Integer spiciness = this.spiciness;\n"
      + "      return new AutoValue_Ingredient(spiciness);\n"
      + "    }\n"
      + "    public Integer getSpiciness() {\n"
      + "      return spiciness;\n"
      + "    }\n"
      + "  }\n"
      + "}\n");

    assertAbout(javaSources())
      .that(Arrays.asList(EXCLUDE, source))
      .processedWith(new AutoValueProcessor())
      .compilesWithoutError()
      .and()
      .generatesSources(expected);
  }

  @Test
  public void string() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Review",
      "package test;\n"
      + "\n"
      + "import com.google.auto.value.AutoValue;\n"
      + "import me.mattlogan.auto.value.firebase.annotation.FirebaseValue;\n"
      + "\n"
      + "@AutoValue @FirebaseValue\n"
      + "public abstract class Review {\n"
      + "  public abstract String description();\n"
      + "}\n");

    JavaFileObject expected = JavaFileObjects.forSourceString("test.AutoValue_Review",
      "package test;\n"
      + "\n"
      + "import com.google.firebase.database.Exclude;\n"
      + "import java.lang.String;\n"
      + "import java.lang.SuppressWarnings;\n"
      + "\n"
      + "final class AutoValue_Review extends $AutoValue_Review {\n"
      + "  AutoValue_Review(String description) {\n"
      + "    super(description);\n"
      + "  }\n"
      + "\n"
      + "  static final class FirebaseValue {\n"
      + "    private String description;\n"
      + "    @SuppressWarnings(\"unused\")\n"
      + "    FirebaseValue() {\n"
      + "    }\n"
      + "    FirebaseValue(Review review) {\n"
      + "      this.description = review.description();\n"
      + "    }\n"
      + "    @Exclude\n"
      + "    AutoValue_Review toAutoValue() {\n"
      + "      String description = this.description;\n"
      + "      return new AutoValue_Review(description);\n"
      + "    }\n"
      + "    public String getDescription() {\n"
      + "      return description;\n"
      + "    }\n"
      + "  }\n"
      + "}\n");

    assertAbout(javaSources())
      .that(Arrays.asList(EXCLUDE, source))
      .processedWith(new AutoValueProcessor())
      .compilesWithoutError()
      .and()
      .generatesSources(expected);
  }

  @Test
  public void customObject() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Taco",
      "package test;\n"
      + "\n"
      + "import com.google.auto.value.AutoValue;\n"
      + "import me.mattlogan.auto.value.firebase.annotation.FirebaseValue;\n"
      + "\n"
      + "@AutoValue @FirebaseValue\n"
      + "public abstract class Taco {\n"
      + "  public abstract Ingredient ingredient();\n"
      + "}\n");

    JavaFileObject expected = JavaFileObjects.forSourceString("test.AutoValue_Taco",
      "package test;\n"
      + "\n"
      + "import com.google.firebase.database.Exclude;\n"
      + "import java.lang.SuppressWarnings;\n"
      + "\n"
      + "final class AutoValue_Taco extends $AutoValue_Taco {\n"
      + "  AutoValue_Taco(Ingredient ingredient) {\n"
      + "    super(ingredient);\n"
      + "  }\n"
      + "\n"
      + "  static final class FirebaseValue {\n"
      + "    private AutoValue_Ingredient.FirebaseValue ingredient;\n"
      + "    @SuppressWarnings(\"unused\")\n"
      + "    FirebaseValue() {\n"
      + "    }\n"
      + "    FirebaseValue(Taco taco) {\n"
      + "      this.ingredient = taco.ingredient() == null ? null : new AutoValue_Ingredient.FirebaseValue(taco.ingredient());\n"
      + "    }\n"
      + "    @Exclude\n"
      + "    AutoValue_Taco toAutoValue() {\n"
      + "      Ingredient ingredient = this.ingredient == null ? null : this.ingredient.toAutoValue();\n"
      + "      return new AutoValue_Taco(ingredient);\n"
      + "    }\n"
      + "    public AutoValue_Ingredient.FirebaseValue getIngredient() {\n"
      + "      return ingredient;\n"
      + "    }\n"
      + "  }\n"
      + "}\n");

    assertAbout(javaSources())
      .that(Arrays.asList(EXCLUDE, INGREDIENT, source))
      .processedWith(new AutoValueProcessor())
      .compilesWithoutError()
      .and()
      .generatesSources(expected);
  }

  @Test
  public void boxedPrimitiveList() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Taco",
      "package test;\n"
      + "\n"
      + "import com.google.auto.value.AutoValue;\n"
      + "import java.util.List;\n"
      + "import me.mattlogan.auto.value.firebase.annotation.FirebaseValue;\n"
      + "\n"
      + "@AutoValue @FirebaseValue\n"
      + "public abstract class Taco {\n"
      + "  public abstract List<Integer> ingredients();\n"
      + "}\n");

    JavaFileObject expected = JavaFileObjects.forSourceString("test.AutoValue_Taco",
      "package test;\n"
      + "\n"
      + "import com.google.firebase.database.Exclude;\n"
      + "import java.lang.Integer;\n"
      + "import java.lang.SuppressWarnings;\n"
      + "import java.util.List;\n"
      + "\n"
      + "final class AutoValue_Taco extends $AutoValue_Taco {\n"
      + "  AutoValue_Taco(List<Integer> ingredients) {\n"
      + "    super(ingredients);\n"
      + "  }\n"
      + "\n"
      + "  static final class FirebaseValue {\n"
      + "    private List<Integer> ingredients;\n"
      + "    @SuppressWarnings(\"unused\")\n"
      + "    FirebaseValue() {\n"
      + "    }\n"
      + "    FirebaseValue(Taco taco) {\n"
      + "      this.ingredients = taco.ingredients();\n"
      + "    }\n"
      + "    @Exclude\n"
      + "    AutoValue_Taco toAutoValue() {\n"
      + "      List<Integer> ingredients = this.ingredients;\n"
      + "      return new AutoValue_Taco(ingredients);\n"
      + "    }\n"
      + "    public List<Integer> getIngredients() {\n"
      + "      return ingredients;\n"
      + "    }\n"
      + "  }\n"
      + "}\n");

    assertAbout(javaSources())
      .that(Arrays.asList(EXCLUDE, source))
      .processedWith(new AutoValueProcessor())
      .compilesWithoutError()
      .and()
      .generatesSources(expected);
  }

  @Test
  public void stringList() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Taco",
      "package test;\n"
      + "\n"
      + "import com.google.auto.value.AutoValue;\n"
      + "import java.util.List;\n"
      + "import me.mattlogan.auto.value.firebase.annotation.FirebaseValue;\n"
      + "\n"
      + "@AutoValue @FirebaseValue\n"
      + "public abstract class Taco {\n"
      + "  public abstract List<String> ingredients();\n"
      + "}\n");

    JavaFileObject expected = JavaFileObjects.forSourceString("test.AutoValue_Taco",
      "package test;\n"
      + "\n"
      + "import com.google.firebase.database.Exclude;\n"
      + "import java.lang.String;\n"
      + "import java.lang.SuppressWarnings;\n"
      + "import java.util.List;\n"
      + "\n"
      + "final class AutoValue_Taco extends $AutoValue_Taco {\n"
      + "  AutoValue_Taco(List<String> ingredients) {\n"
      + "    super(ingredients);\n"
      + "  }\n"
      + "\n"
      + "  static final class FirebaseValue {\n"
      + "    private List<String> ingredients;\n"
      + "    @SuppressWarnings(\"unused\")\n"
      + "    FirebaseValue() {\n"
      + "    }\n"
      + "    FirebaseValue(Taco taco) {\n"
      + "      this.ingredients = taco.ingredients();\n"
      + "    }\n"
      + "    @Exclude\n"
      + "    AutoValue_Taco toAutoValue() {\n"
      + "      List<String> ingredients = this.ingredients;\n"
      + "      return new AutoValue_Taco(ingredients);\n"
      + "    }\n"
      + "    public List<String> getIngredients() {\n"
      + "      return ingredients;\n"
      + "    }\n"
      + "  }\n"
      + "}\n");

    assertAbout(javaSources())
      .that(Arrays.asList(EXCLUDE, source))
      .processedWith(new AutoValueProcessor())
      .compilesWithoutError()
      .and()
      .generatesSources(expected);
  }

  @Test
  public void customObjectList() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Taco",
      "package test;\n"
      + "\n"
      + "import com.google.auto.value.AutoValue;\n"
      + "import java.util.List;\n"
      + "import me.mattlogan.auto.value.firebase.annotation.FirebaseValue;\n"
      + "\n"
      + "@AutoValue @FirebaseValue\n"
      + "public abstract class Taco {\n"
      + "  public abstract List<Ingredient> ingredients();\n"
      + "}\n");

    JavaFileObject expected = JavaFileObjects.forSourceString("test.AutoValue_Taco",
      "package test;\n"
      + "\n"
      + "import com.google.firebase.database.Exclude;\n"
      + "import java.lang.SuppressWarnings;\n"
      + "import java.util.ArrayList;\n"
      + "import java.util.List;\n"
      + "\n"
      + "final class AutoValue_Taco extends $AutoValue_Taco {\n"
      + "  AutoValue_Taco(List<Ingredient> ingredients) {\n"
      + "    super(ingredients);\n"
      + "  }\n"
      + "\n"
      + "  static final class FirebaseValue {\n"
      + "    private List<AutoValue_Ingredient.FirebaseValue> ingredients;\n"
      + "    @SuppressWarnings(\"unused\")\n"
      + "    FirebaseValue() {\n"
      + "    }\n"
      + "    FirebaseValue(Taco taco) {\n"
      + "      if (taco.ingredients() != null) {\n"
      + "        this.ingredients = new ArrayList<>();\n"
      + "        for (Ingredient item : taco.ingredients()) {\n"
      + "          this.ingredients.add(new AutoValue_Ingredient.FirebaseValue(item));\n"
      + "        }\n"
      + "      }\n"
      + "    }\n"
      + "    @Exclude\n"
      + "    AutoValue_Taco toAutoValue() {\n"
      + "      List<Ingredient> ingredients = null;\n"
      + "      if (this.ingredients != null) {\n"
      + "        ingredients = new ArrayList<>();\n"
      + "        for (AutoValue_Ingredient.FirebaseValue item : this.ingredients) {\n"
      + "          ingredients.add(item.toAutoValue());\n"
      + "        }\n"
      + "      }\n"
      + "      return new AutoValue_Taco(ingredients);\n"
      + "    }\n"
      + "    public List<AutoValue_Ingredient.FirebaseValue> getIngredients() {\n"
      + "      return ingredients;\n"
      + "    }\n"
      + "  }\n"
      + "}\n");

    assertAbout(javaSources())
      .that(Arrays.asList(EXCLUDE, INGREDIENT, source))
      .processedWith(new AutoValueProcessor())
      .compilesWithoutError()
      .and()
      .generatesSources(expected);
  }

  @Test
  public void boxedPrimitiveMap() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Taco",
      "package test;\n"
      + "\n"
      + "import com.google.auto.value.AutoValue;\n"
      + "import java.util.Map;\n"
      + "import me.mattlogan.auto.value.firebase.annotation.FirebaseValue;\n"
      + "\n"
      + "@AutoValue @FirebaseValue\n"
      + "public abstract class Taco {\n"
      + "  public abstract Map<Integer, Integer> ingredients();\n"
      + "}\n");

    JavaFileObject expected = JavaFileObjects.forSourceString("test.AutoValue_Taco",
      "package test;\n"
      + "\n"
      + "import com.google.firebase.database.Exclude;\n"
      + "import java.lang.Integer;\n"
      + "import java.lang.SuppressWarnings;\n"
      + "import java.util.Map;\n"
      + "\n"
      + "final class AutoValue_Taco extends $AutoValue_Taco {\n"
      + "  AutoValue_Taco(Map<Integer, Integer> ingredients) {\n"
      + "    super(ingredients);\n"
      + "  }\n"
      + "\n"
      + "  static final class FirebaseValue {\n"
      + "    private Map<Integer, Integer> ingredients;\n"
      + "    @SuppressWarnings(\"unused\")\n"
      + "    FirebaseValue() {\n"
      + "    }\n"
      + "    FirebaseValue(Taco taco) {\n"
      + "      this.ingredients = taco.ingredients();\n"
      + "    }\n"
      + "    @Exclude\n"
      + "    AutoValue_Taco toAutoValue() {\n"
      + "      Map<Integer, Integer> ingredients = this.ingredients;\n"
      + "      return new AutoValue_Taco(ingredients);\n"
      + "    }\n"
      + "    public Map<Integer, Integer> getIngredients() {\n"
      + "      return ingredients;\n"
      + "    }\n"
      + "  }\n"
      + "}\n");

    assertAbout(javaSources())
      .that(Arrays.asList(EXCLUDE, source))
      .processedWith(new AutoValueProcessor())
      .compilesWithoutError()
      .and()
      .generatesSources(expected);
  }

  @Test
  public void stringMap() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Taco",
      "package test;\n"
      + "\n"
      + "import com.google.auto.value.AutoValue;\n"
      + "import java.util.Map;\n"
      + "import me.mattlogan.auto.value.firebase.annotation.FirebaseValue;\n"
      + "\n"
      + "@AutoValue @FirebaseValue\n"
      + "public abstract class Taco {\n"
      + "  public abstract Map<Integer, String> ingredients();\n"
      + "}\n");

    JavaFileObject expected = JavaFileObjects.forSourceString("test.AutoValue_Taco",
      "package test;\n"
      + "\n"
      + "import com.google.firebase.database.Exclude;\n"
      + "import java.lang.Integer;\n"
      + "import java.lang.String;\n"
      + "import java.lang.SuppressWarnings;\n"
      + "import java.util.Map;\n"
      + "\n"
      + "final class AutoValue_Taco extends $AutoValue_Taco {\n"
      + "  AutoValue_Taco(Map<Integer, String> ingredients) {\n"
      + "    super(ingredients);\n"
      + "  }\n"
      + "\n"
      + "  static final class FirebaseValue {\n"
      + "    private Map<Integer, String> ingredients;\n"
      + "    @SuppressWarnings(\"unused\")\n"
      + "    FirebaseValue() {\n"
      + "    }\n"
      + "    FirebaseValue(Taco taco) {\n"
      + "      this.ingredients = taco.ingredients();\n"
      + "    }\n"
      + "    @Exclude\n"
      + "    AutoValue_Taco toAutoValue() {\n"
      + "      Map<Integer, String> ingredients = this.ingredients;\n"
      + "      return new AutoValue_Taco(ingredients);\n"
      + "    }\n"
      + "    public Map<Integer, String> getIngredients() {\n"
      + "      return ingredients;\n"
      + "    }\n"
      + "  }\n"
      + "}\n");

    assertAbout(javaSources())
      .that(Arrays.asList(EXCLUDE, source))
      .processedWith(new AutoValueProcessor())
      .compilesWithoutError()
      .and()
      .generatesSources(expected);
  }

  @Test
  public void customObjectMap() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Taco",
      "package test;\n"
      + "\n"
      + "import com.google.auto.value.AutoValue;\n"
      + "import java.util.Map;\n"
      + "import me.mattlogan.auto.value.firebase.annotation.FirebaseValue;\n"
      + "\n"
      + "@AutoValue @FirebaseValue\n"
      + "public abstract class Taco {\n"
      + "  public abstract Map<String, Ingredient> ingredients();\n"
      + "}\n");

    JavaFileObject expected = JavaFileObjects.forSourceString("test.AutoValue_Taco",
      "package test;\n"
      + "\n"
      + "import com.google.firebase.database.Exclude;\n"
      + "import java.lang.String;\n"
      + "import java.lang.SuppressWarnings;\n"
      + "import java.util.HashMap;\n"
      + "import java.util.Map;\n"
      + "\n"
      + "final class AutoValue_Taco extends $AutoValue_Taco {\n"
      + "  AutoValue_Taco(Map<String, Ingredient> ingredients) {\n"
      + "    super(ingredients);\n"
      + "  }\n"
      + "\n"
      + "  static final class FirebaseValue {\n"
      + "    private Map<String, AutoValue_Ingredient.FirebaseValue> ingredients;\n"
      + "    @SuppressWarnings(\"unused\")\n"
      + "    FirebaseValue() {\n"
      + "    }\n"
      + "    FirebaseValue(Taco taco) {\n"
      + "      if (taco.ingredients() != null) {\n"
      + "        this.ingredients = new HashMap<>();\n"
      + "        for (Map.Entry<String, Ingredient> entry : taco.ingredients().entrySet()) {\n"
      + "          this.ingredients.put(entry.getKey(), new AutoValue_Ingredient.FirebaseValue(entry.getValue()));\n"
      + "        }\n"
      + "      }\n"
      + "    }\n"
      + "    @Exclude\n"
      + "    AutoValue_Taco toAutoValue() {\n"
      + "      Map<String, Ingredient> ingredients = null;\n"
      + "      if (this.ingredients != null) {\n"
      + "        ingredients = new HashMap<>();\n"
      + "        for (Map.Entry<String, AutoValue_Ingredient.FirebaseValue> entry : this.ingredients.entrySet()) {\n"
      + "          ingredients.put(entry.getKey(), entry.getValue().toAutoValue());\n"
      + "        }\n"
      + "      }\n"
      + "      return new AutoValue_Taco(ingredients);\n"
      + "    }\n"
      + "    public Map<String, AutoValue_Ingredient.FirebaseValue> getIngredients() {\n"
      + "      return ingredients;\n"
      + "    }\n"
      + "  }\n"
      + "}\n");

    assertAbout(javaSources())
      .that(Arrays.asList(EXCLUDE, INGREDIENT, source))
      .processedWith(new AutoValueProcessor())
      .compilesWithoutError()
      .and()
      .generatesSources(expected);
  }

  @Test
  public void multipleProperties() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Taco",
      "package test;\n"
      + "\n"
      + "import com.google.auto.value.AutoValue;\n"
      + "import java.util.List;\n"
      + "import me.mattlogan.auto.value.firebase.annotation.FirebaseValue;\n"
      + "\n"
      + "@AutoValue @FirebaseValue\n"
      + "public abstract class Taco {\n"
      + "  public abstract String name();\n"
      + "  public abstract List<Ingredient> ingredients();\n"
      + "  public abstract Review review();\n"
      + "}\n");

    JavaFileObject expected = JavaFileObjects.forSourceString("test.AutoValue_Taco",
      "package test;\n"
      + "\n"
      + "import com.google.firebase.database.Exclude;\n"
      + "import java.lang.String;\n"
      + "import java.lang.SuppressWarnings;\n"
      + "import java.util.ArrayList;\n"
      + "import java.util.List;\n"
      + "\n"
      + "final class AutoValue_Taco extends $AutoValue_Taco {\n"
      + "  AutoValue_Taco(String name, List<Ingredient> ingredients, Review review) {\n"
      + "    super(name, ingredients, review);\n"
      + "  }\n"
      + "\n"
      + "  static final class FirebaseValue {\n"
      + "    private String name;\n"
      + "    private List<AutoValue_Ingredient.FirebaseValue> ingredients;\n"
      + "    private AutoValue_Review.FirebaseValue review;\n"
      + "    @SuppressWarnings(\"unused\")\n"
      + "    FirebaseValue() {\n"
      + "    }\n"
      + "    FirebaseValue(Taco taco) {\n"
      + "      this.name = taco.name();\n"
      + "      if (taco.ingredients() != null) {\n"
      + "        this.ingredients = new ArrayList<>();\n"
      + "        for (Ingredient item : taco.ingredients()) {\n"
      + "          this.ingredients.add(new AutoValue_Ingredient.FirebaseValue(item));\n"
      + "        }\n"
      + "      }\n"
      + "      this.review = taco.review() == null ? null : new AutoValue_Review.FirebaseValue(taco.review());\n"
      + "    }\n"
      + "    @Exclude\n"
      + "    AutoValue_Taco toAutoValue() {\n"
      + "      String name = this.name;\n"
      + "      List<Ingredient> ingredients = null;\n"
      + "      if (this.ingredients != null) {\n"
      + "        ingredients = new ArrayList<>();\n"
      + "        for (AutoValue_Ingredient.FirebaseValue item : this.ingredients) {\n"
      + "          ingredients.add(item.toAutoValue());\n"
      + "        }\n"
      + "      }\n"
      + "      Review review = this.review == null ? null : this.review.toAutoValue();\n"
      + "      return new AutoValue_Taco(name, ingredients, review);\n"
      + "    }\n"
      + "    public String getName() {\n"
      + "      return name;\n"
      + "    }\n"
      + "    public List<AutoValue_Ingredient.FirebaseValue> getIngredients() {\n"
      + "      return ingredients;\n"
      + "    }\n"
      + "    public AutoValue_Review.FirebaseValue getReview() {\n"
      + "      return review;\n"
      + "    }\n"
      + "  }\n"
      + "}\n");

    assertAbout(javaSources())
      .that(Arrays.asList(EXCLUDE, INGREDIENT, REVIEW, source))
      .processedWith(new AutoValueProcessor())
      .compilesWithoutError()
      .and()
      .generatesSources(expected);
  }

  @Test
  public void excludeAnnotation() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Ingredient",
      "package test;\n"
      + "\n"
      + "import com.google.auto.value.AutoValue;\n"
      + "import com.google.firebase.database.Exclude;\n"
      + "import me.mattlogan.auto.value.firebase.annotation.FirebaseValue;\n"
      + "\n"
      + "@AutoValue @FirebaseValue\n"
      + "public abstract class Ingredient {\n"
      + "  @Exclude\n"
      + "  public abstract int spiciness();\n"
      + "}\n");

    JavaFileObject expected = JavaFileObjects.forSourceString("test.AutoValue_Ingredient",
      "package test;\n"
      + "\n"
      + "import com.google.firebase.database.Exclude;\n"
      + "import java.lang.SuppressWarnings;\n"
      + "\n"
      + "final class AutoValue_Ingredient extends $AutoValue_Ingredient {\n"
      + "  AutoValue_Ingredient(int spiciness) {\n"
      + "    super(spiciness);\n"
      + "  }\n"
      + "\n"
      + "  static final class FirebaseValue {\n"
      + "    private int spiciness;\n"
      + "    @SuppressWarnings(\"unused\")\n"
      + "    FirebaseValue() {\n"
      + "    }\n"
      + "    FirebaseValue(Ingredient ingredient) {\n"
      + "      this.spiciness = ingredient.spiciness();\n"
      + "    }\n"
      + "    @Exclude\n"
      + "    AutoValue_Ingredient toAutoValue() {\n"
      + "      int spiciness = this.spiciness;\n"
      + "      return new AutoValue_Ingredient(spiciness);\n"
      + "    }\n"
      + "    @Exclude\n"
      + "    public int getSpiciness() {\n"
      + "      return spiciness;\n"
      + "    }\n"
      + "  }\n"
      + "}\n");

    assertAbout(javaSources())
      .that(Arrays.asList(EXCLUDE, source))
      .processedWith(new AutoValueProcessor())
      .compilesWithoutError()
      .and()
      .generatesSources(expected);
  }

  @Test
  public void propertyNameAnnotation() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Ingredient",
      "package test;\n"
      + "\n"
      + "import com.google.auto.value.AutoValue;\n"
      + "import com.google.firebase.database.PropertyName;\n"
      + "import me.mattlogan.auto.value.firebase.annotation.FirebaseValue;\n"
      + "\n"
      + "@AutoValue @FirebaseValue\n"
      + "public abstract class Ingredient {\n"
      + "  @PropertyName(\"picante\")\n"
      + "  public abstract int spiciness();\n"
      + "}\n");

    JavaFileObject expected = JavaFileObjects.forSourceString("test.AutoValue_Ingredient",
      "package test;\n"
      + "\n"
      + "import com.google.firebase.database.Exclude;\n"
      + "import com.google.firebase.database.PropertyName;\n"
      + "import java.lang.SuppressWarnings;\n"
      + "\n"
      + "final class AutoValue_Ingredient extends $AutoValue_Ingredient {\n"
      + "  AutoValue_Ingredient(int spiciness) {\n"
      + "    super(spiciness);\n"
      + "  }\n"
      + "\n"
      + "  static final class FirebaseValue {\n"
      + "    private int spiciness;\n"
      + "    @SuppressWarnings(\"unused\")\n"
      + "    FirebaseValue() {\n"
      + "    }\n"
      + "    FirebaseValue(Ingredient ingredient) {\n"
      + "      this.spiciness = ingredient.spiciness();\n"
      + "    }\n"
      + "    @Exclude\n"
      + "    AutoValue_Ingredient toAutoValue() {\n"
      + "      int spiciness = this.spiciness;\n"
      + "      return new AutoValue_Ingredient(spiciness);\n"
      + "    }\n"
      + "    @PropertyName(\"picante\")\n"
      + "    public int getSpiciness() {\n"
      + "      return spiciness;\n"
      + "    }\n"
      + "  }\n"
      + "}\n");

    assertAbout(javaSources())
      .that(Arrays.asList(EXCLUDE, PROPERTY_NAME, source))
      .processedWith(new AutoValueProcessor())
      .compilesWithoutError()
      .and()
      .generatesSources(expected);
  }

  @Test
  public void ignoreExtraPropertiesAnnotation() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Ingredient",
      "package test;\n"
      + "\n"
      + "import com.google.auto.value.AutoValue;\n"
      + "import com.google.firebase.database.IgnoreExtraProperties;\n"
      + "import me.mattlogan.auto.value.firebase.annotation.FirebaseValue;\n"
      + "\n"
      + "@AutoValue @FirebaseValue @IgnoreExtraProperties\n"
      + "public abstract class Ingredient {\n"
      + "  public abstract int spiciness();\n"
      + "}\n");

    JavaFileObject expected = JavaFileObjects.forSourceString("test.AutoValue_Ingredient",
      "package test;\n"
      + "\n"
      + "import com.google.firebase.database.Exclude;\n"
      + "import com.google.firebase.database.IgnoreExtraProperties;\n"
      + "import java.lang.SuppressWarnings;\n"
      + "\n"
      + "final class AutoValue_Ingredient extends $AutoValue_Ingredient {\n"
      + "  AutoValue_Ingredient(int spiciness) {\n"
      + "    super(spiciness);\n"
      + "  }\n"
      + "\n"
      + "  @IgnoreExtraProperties\n"
      + "  static final class FirebaseValue {\n"
      + "    private int spiciness;\n"
      + "    @SuppressWarnings(\"unused\")\n"
      + "    FirebaseValue() {\n"
      + "    }\n"
      + "    FirebaseValue(Ingredient ingredient) {\n"
      + "      this.spiciness = ingredient.spiciness();\n"
      + "    }\n"
      + "    @Exclude\n"
      + "    AutoValue_Ingredient toAutoValue() {\n"
      + "      int spiciness = this.spiciness;\n"
      + "      return new AutoValue_Ingredient(spiciness);\n"
      + "    }\n"
      + "    public int getSpiciness() {\n"
      + "      return spiciness;\n"
      + "    }\n"
      + "  }\n"
      + "}\n");

    assertAbout(javaSources())
      .that(Arrays.asList(EXCLUDE, IGNORE_EXTRA_PROPERTIES, source))
      .processedWith(new AutoValueProcessor())
      .compilesWithoutError()
      .and()
      .generatesSources(expected);
  }

  @Test
  public void throwOnExtraPropertiesAnnotation() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Ingredient",
      "package test;\n"
      + "\n"
      + "import com.google.auto.value.AutoValue;\n"
      + "import com.google.firebase.database.ThrowOnExtraProperties;\n"
      + "import me.mattlogan.auto.value.firebase.annotation.FirebaseValue;\n"
      + "\n"
      + "@AutoValue @FirebaseValue @ThrowOnExtraProperties\n"
      + "public abstract class Ingredient {\n"
      + "  public abstract int spiciness();\n"
      + "}\n");

    JavaFileObject expected = JavaFileObjects.forSourceString("test.AutoValue_Ingredient",
      "package test;\n"
      + "\n"
      + "import com.google.firebase.database.Exclude;\n"
      + "import com.google.firebase.database.ThrowOnExtraProperties;\n"
      + "import java.lang.SuppressWarnings;\n"
      + "\n"
      + "final class AutoValue_Ingredient extends $AutoValue_Ingredient {\n"
      + "  AutoValue_Ingredient(int spiciness) {\n"
      + "    super(spiciness);\n"
      + "  }\n"
      + "\n"
      + "  @ThrowOnExtraProperties\n"
      + "  static final class FirebaseValue {\n"
      + "    private int spiciness;\n"
      + "    @SuppressWarnings(\"unused\")\n"
      + "    FirebaseValue() {\n"
      + "    }\n"
      + "    FirebaseValue(Ingredient ingredient) {\n"
      + "      this.spiciness = ingredient.spiciness();\n"
      + "    }\n"
      + "    @Exclude\n"
      + "    AutoValue_Ingredient toAutoValue() {\n"
      + "      int spiciness = this.spiciness;\n"
      + "      return new AutoValue_Ingredient(spiciness);\n"
      + "    }\n"
      + "    public int getSpiciness() {\n"
      + "      return spiciness;\n"
      + "    }\n"
      + "  }\n"
      + "}\n");

    assertAbout(javaSources())
      .that(Arrays.asList(EXCLUDE, THROW_ON_EXTRA_PROPERTIES, source))
      .processedWith(new AutoValueProcessor())
      .compilesWithoutError()
      .and()
      .generatesSources(expected);
  }

  @Test
  public void listOfParameterizedTypes() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Taco",
      "package test;\n"
      + "\n"
      + "import com.google.auto.value.AutoValue;\n"
      + "import java.util.List;\n"
      + "import me.mattlogan.auto.value.firebase.annotation.FirebaseValue;\n"
      + "\n"
      + "@AutoValue @FirebaseValue\n"
      + "public abstract class Taco {\n"
      + "  public abstract List<List<String>> ingredients();\n"
      + "}\n");

    assertAbout(javaSources())
      .that(Arrays.asList(EXCLUDE, source))
      .processedWith(new AutoValueProcessor())
      .failsToCompile();
  }

  @Test
  public void mapWithNonPrimitiveKeyTypes() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Taco",
      "package test;\n"
      + "\n"
      + "import com.google.auto.value.AutoValue;\n"
      + "import java.util.Map;\n"
      + "import me.mattlogan.auto.value.firebase.annotation.FirebaseValue;\n"
      + "\n"
      + "@AutoValue @FirebaseValue\n"
      + "public abstract class Taco {\n"
      + "  public abstract Map<Ingredient, Integer> ingredients();\n"
      + "}\n");

    assertAbout(javaSources())
      .that(Arrays.asList(EXCLUDE, source))
      .processedWith(new AutoValueProcessor())
      .failsToCompile();
  }

  @Test
  public void mapWithParemeterizedValueTypes() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Taco",
      "package test;\n"
      + "\n"
      + "import com.google.auto.value.AutoValue;\n"
      + "import java.util.List;\n"
      + "import java.util.Map;\n"
      + "import me.mattlogan.auto.value.firebase.annotation.FirebaseValue;\n"
      + "\n"
      + "@AutoValue @FirebaseValue\n"
      + "public abstract class Taco {\n"
      + "  public abstract Map<Integer, List<Ingredient>> ingredients();\n"
      + "}\n");

    assertAbout(javaSources())
      .that(Arrays.asList(EXCLUDE, source))
      .processedWith(new AutoValueProcessor())
      .failsToCompile();
  }

  @Test
  public void set() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Taco",
      "package test;\n"
      + "\n"
      + "import com.google.auto.value.AutoValue;\n"
      + "import java.util.Set;\n"
      + "import me.mattlogan.auto.value.firebase.annotation.FirebaseValue;\n"
      + "\n"
      + "@AutoValue @FirebaseValue\n"
      + "public abstract class Taco {\n"
      + "  public abstract Set<Ingredient> ingredients();\n"
      + "}\n");

    assertAbout(javaSources())
      .that(Arrays.asList(EXCLUDE, source))
      .processedWith(new AutoValueProcessor())
      .failsToCompile();
  }

  @Test
  public void arrayList() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Taco",
      "package test;\n"
      + "\n"
      + "import com.google.auto.value.AutoValue;\n"
      + "import java.util.ArrayList;\n"
      + "import me.mattlogan.auto.value.firebase.annotation.FirebaseValue;\n"
      + "\n"
      + "@AutoValue @FirebaseValue\n"
      + "public abstract class Taco {\n"
      + "  public abstract ArrayList<Ingredient> ingredients();\n"
      + "}\n");

    assertAbout(javaSources())
      .that(Arrays.asList(EXCLUDE, source))
      .processedWith(new AutoValueProcessor())
      .failsToCompile();
  }

  @Test
  public void typeAdapterEnum() throws Exception {
    JavaFileObject typeAdapterSource = JavaFileObjects.forSourceLines("test.StatusAdapter",
      "package test;\n" +
        "\n" +
        "import me.mattlogan.auto.value.firebase.adapter.TypeAdapter;\n" +
        "import test.Taco.Status;\n"+
        "\n" +
        "public class StatusAdapter implements TypeAdapter<Status, String> {\n" +
        "  @Override\n" +
        "  public Status fromFirebaseValue(String value) {\n" +
        "    if(\"cooked\".equals(value)){\n" +
        "      return Status.COOKED;\n" +
        "    }\n" +
        "    else if(\"uncooked\".equals(value)){\n" +
        "      return Status.UNCOOKED;\n" +
        "    }\n" +
        "    else {\n" +
        "      throw new IllegalStateException(\"unsupported\");\n" +
        "    }\n" +
        "  }\n" +
        "\n" +
        "  @Override\n" +
        "  public String toFirebaseValue(Status value) {\n" +
        "    switch (value){\n" +
        "      case COOKED:\n" +
        "        return \"cooked\";\n" +
        "      case UNCOOKED:\n" +
        "        return \"uncooked\";\n" +
        "      default:\n" +
        "        throw new IllegalStateException(\"unsupported\");\n" +
        "    }\n" +
        "  }\n" +
        "}");
    JavaFileObject source = JavaFileObjects.forSourceString("test.Taco",
      "package test;\n"
        + "\n"
        + "import com.google.auto.value.AutoValue;\n"
        + "import java.util.ArrayList;\n"
        + "import me.mattlogan.auto.value.firebase.annotation.FirebaseValue;\n"
        + "import me.mattlogan.auto.value.firebase.adapter.FirebaseAdapter;\n"
        + "\n"
        + "@AutoValue @FirebaseValue\n"
        + "public abstract class Taco {\n"
        + "  enum Status { UNCOOKED, COOKED }; "
        + "  @FirebaseAdapter(StatusAdapter.class) abstract Status status();"
        + "}\n");

    JavaFileObject expectedOutput = JavaFileObjects.forSourceString("test.AutoValue_Taco",
      "package test;\n" +
        "\n" +
        "import com.google.firebase.database.Exclude;\n" +
        "import java.lang.String;\n" +
        "import java.lang.SuppressWarnings;" +
        "final class AutoValue_Taco extends $AutoValue_Taco {\n" +
        "\n" +
        "  AutoValue_TestTaco(Taco.Status status) {\n" +
        "    super(status);\n" +
        "  }\n" +
        "\n" +
        "  static final class FirebaseValue {\n" +
        "    private String status;\n" +
        "    private final StatusAdapter statusAdapter = new StatusAdapter();\n" +
        "    @SuppressWarnings(\"unused\")\n" +
        "    FirebaseValue() {\n" +
        "    }\n" +
        "    FirebaseValue(Taco taco) {\n" +
        "      this.status = taco.status() == null ? null : statusAdapter.toFirebaseValue(taco.status());\n" +
        "    }\n" +
        "    @Exclude\n" +
        "    AutoValue_Taco toAutoValue() {\n" +
        "      Taco.Status status = this.status == null ? null : statusAdapter.fromFirebaseValue(this.status);\n" +
        "      return new AutoValue_Taco(status);\n" +
        "    }\n" +
        "    public String getStatus() {\n" +
        "      return status;\n" +
        "    }\n" +
        "  }\n" +
        "}");
    assertAbout(javaSources())
      .that(Arrays.asList(EXCLUDE, typeAdapterSource, source))
      .processedWith(new AutoValueProcessor())
      .compilesWithoutError()
      .and()
      .generatesSources(expectedOutput);
  }

  @Test
  public void typeAdapterEnumDuplicateAdapter() throws Exception {
    JavaFileObject typeAdapterSource = JavaFileObjects.forSourceLines("test.StatusAdapter",
      "package test;\n" +
        "\n" +
        "import me.mattlogan.auto.value.firebase.adapter.TypeAdapter;\n" +
        "import test.Taco.Status;\n" +
        "\n" +
        "public class StatusAdapter implements TypeAdapter<Status, String> {\n" +
        "  @Override\n" +
        "  public Status fromFirebaseValue(String value) {\n" +
        "    if(\"cooked\".equals(value)){\n" +
        "      return Status.COOKED;\n" +
        "    }\n" +
        "    else if(\"uncooked\".equals(value)){\n" +
        "      return Status.UNCOOKED;\n" +
        "    }\n" +
        "    else {\n" +
        "      throw new IllegalStateException(\"unsupported\");\n" +
        "    }\n" +
        "  }\n" +
        "\n" +
        "  @Override\n" +
        "  public String toFirebaseValue(Status value) {\n" +
        "    switch (value){\n" +
        "      case COOKED:\n" +
        "        return \"cooked\";\n" +
        "      case UNCOOKED:\n" +
        "        return \"uncooked\";\n" +
        "      default:\n" +
        "        throw new IllegalStateException(\"unsupported\");\n" +
        "    }\n" +
        "  }\n" +
        "}");
    JavaFileObject source = JavaFileObjects.forSourceString("test.Taco",
      "package test;\n"
        + "\n"
        + "import com.google.auto.value.AutoValue;\n"
        + "import java.util.ArrayList;\n"
        + "import me.mattlogan.auto.value.firebase.annotation.FirebaseValue;\n"
        + "import me.mattlogan.auto.value.firebase.adapter.FirebaseAdapter;\n"
        + "\n"
        + "@AutoValue @FirebaseValue\n"
        + "public abstract class Taco {\n"
        + "  enum Status { UNCOOKED, COOKED }; "
        + "  @FirebaseAdapter(StatusAdapter.class) abstract Status status();"
        + "  @FirebaseAdapter(StatusAdapter.class) abstract Status secondaryStatus();"
        + "}\n");

    JavaFileObject expectedOutput = JavaFileObjects.forSourceString("test.AutoValue_Taco",
      "package test;\n" +
        "\n" +
        "import com.google.firebase.database.Exclude;\n" +
        "import java.lang.String;\n" +
        "import java.lang.SuppressWarnings;" +
        "final class AutoValue_Taco extends $AutoValue_Taco {\n" +
        "\n" +
        "  AutoValue_TestTaco(Taco.Status status, Taco.Status secondaryStatus) {\n" +
        "    super(status, secondaryStatus);\n" +
        "  }\n" +
        "\n" +
        "  static final class FirebaseValue {\n" +
        "    private String status;\n" +
        "    private String secondaryStatus;\n"+
        "    private final StatusAdapter statusAdapter = new StatusAdapter();\n" +
        "    @SuppressWarnings(\"unused\")\n" +
        "    FirebaseValue() {\n" +
        "    }\n" +
        "    FirebaseValue(Taco taco) {\n" +
        "      this.status = taco.status() == null ? null : statusAdapter.toFirebaseValue(taco.status());\n" +
        "      this.secondaryStatus = taco.secondaryStatus() == null ? null: statusAdapter.toFirebaseValue(taco.secondaryStatus());\n"+
        "    }\n" +
        "    @Exclude\n" +
        "    AutoValue_Taco toAutoValue() {\n" +
        "      Taco.Status status = this.status == null ? null : statusAdapter.fromFirebaseValue(this.status);\n" +
        "      Taco.Status secondaryStatus = this.secondaryStatus == null ? null : statusAdapter.fromFirebaseValue(this.secondaryStatus);\n" +
        "      return new AutoValue_Taco(status, secondaryStatus);\n" +
        "    }\n" +
        "    public String getStatus() {\n" +
        "      return status;\n" +
        "    }\n" +
        "    public String getSecondaryStatus() {\n" +
        "      return secondaryStatus;\n" +
        "    }\n" +
        "  }\n" +
        "}");
    assertAbout(javaSources())
      .that(Arrays.asList(EXCLUDE, typeAdapterSource, source))
      .processedWith(new AutoValueProcessor())
      .compilesWithoutError()
      .and()
      .generatesSources(expectedOutput);
  }
}
