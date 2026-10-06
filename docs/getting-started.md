# Getting Started

This page shows how to run LambdaCalc and try your first expressions.

## Prerequisites

LambdaCalc is written in **Scala 3** and is built and run with
[scala-cli](https://scala-cli.virtuslab.org/). It depends on
[JLine](https://github.com/jline/jline3) for interactive terminal input, which
scala-cli downloads automatically from the directive at the top of
`src/main.scala`:

```scala
//> using dep "org.jline:jline:4.4.6"
```

You only need `scala-cli` installed. The dependency is fetched on first run.

## Run the calculator

::: steps

1.  **Run from the repository root**
    Invoke the project with scala-cli:

    ```bash
    scala-cli run .
    ```

2.  **Enter an expression**
    You will see the prompt. Type an equation and press enter:

    ```text
    Calculator started, enter an equation to solve (empty line to quit):
    type :settings for the settings, :visu to toggle the lambda diagrams
    > 
    ```

3.  **Read the three output lines**
    LambdaCalc prints the encoded term, the normal form, and the decimal:

    ```text
    > 6+14*5
    = (λm.λn.λf.λx. m f (n f x))(λf.λx.f(f(f(f(f(f x))))))(λf.λx.f(f(...f x...)))
    = λf.λx.f(f(...f x...))
    = 76
    ```

4.  **Quit**
    Enter an empty line, or send EOF / Ctrl-C.

:::

The three output lines are:

| Line | Meaning |
| --- | --- |
| `= (λm...)` | the **encoded lambda term** produced by the converter |
| `= λf.λx...` | the **β-normal form** after solving it |
| `= 76` | the **decimal number** decoded from the normal form |

## Try these first

| Input | What it does |
| --- | --- |
| `6+14*5` | Multiplication binds tighter than addition → `76` |
| `2^8` | Power is right-associative → `256` |
| `(1+2)*3` | Parentheses override precedence → `9` |
| `2+3^2` | `^` before `+` → `11` |
| `$true` | Treats `true` as the Church boolean |
| `$false^$true` | β-reduction on raw atoms (see the [dollar syntax guide](/guides/dollar-syntax)) |

## Startup flags

Settings can also be passed on the command line, for example:

```bash
scala-cli run . -- --visu --steps --manual --delay 300
```

| Flag | Meaning |
| --- | --- |
| `--visu`, `-v` | Start in lambda-diagram mode |
| `--steps` | Show every β-reduction step |
| `--auto` | Advance steps automatically |
| `--manual` | Wait for enter between steps |
| `--delay <ms>` | Delay between automatic steps |

::: callout warning "The `--` separator matters"
scala-cli consumes its own options first. Pass program flags after `--`, exactly
as shown above.
:::

## Next steps

::: grids
    ::: grid
        ::: card "Concepts" icon:book-open href:/concepts/lambda-calculus
        Learn the theory behind the calculator.
        :::
    :::
    ::: grid
        ::: card "Guides" icon:wrench href:/guides/commands
        Discover the interactive `:` commands.
        :::
    :::
:::
