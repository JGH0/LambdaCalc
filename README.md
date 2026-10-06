# LambdaCalc

> The calculator that can theoretically do anything.

LambdaCalc is a command-line calculator that does **not** calculate the way you
would expect. Instead of evaluating arithmetic directly, it translates a normal
mathematical expression into the **lambda calculus**, then solves it by
**β-reduction**.

So `6+14*5` becomes a tree of Church numerals and Church encodings of addition
and multiplication, which is then reduced back to a normal form that can be
decoded into a decimal number.

## Documentation

Full documentation is published on GitHub Pages:

### **https://jgh0.github.io/LambdaCalc/**

## Requirements

- [scala-cli](https://scala-cli.virtuslab.org/) (Scala 3)
- No other setup: the only dependency ([JLine](https://github.com/jline/jline3))
  is fetched automatically on first run.

## Usage

Run the calculator from the repository root:

```bash
scala-cli run .
```

Enter an equation and press enter; an empty line quits.

```text
> 6+14*5
= (λm.λn.λf.λx. m f (n f x))(λf.λx.f(f(f(f(f(f x))))))(λf.λx.f(f(...f x...)))
= λf.λx.f(f(...f x...))
= 76
```

The three lines are the encoded lambda term, its β-normal form, and the decoded
decimal number.

### Precedence

Multiplication binds tighter than addition, so `6+14*5` first does `14*5` and
then adds `6`. `^` is right-associative, and parentheses override precedence:

```text
6+14*5   -> 76
2^8      -> 256
(1+2)*3  -> 9
2+3^2    -> 11
```

### The `$` escape syntax

Use `$` to mark the following token as a literal atom instead of a number. This
mostly produces data that is unusable as a number, but it can be β-reduced:

```text
$false^$true   false is "true times multiplied"
$++$+          + added to +
$*+$false      multiplication summed with false
```

`$true` and `$false` map to Church booleans. See the
[`$` syntax guide](https://jgh0.github.io/LambdaCalc/guides/dollar-syntax).

## Lambda diagrams

Start with `--visu` (or type `:visu` while running) to draw terms as
[John Tromp lambda diagrams](https://tromp.github.io/cl/diagrams.html) instead of
solving them. Horizontal lines are abstractions, vertical lines are variables, and
the bars between them are applications.

## Reduction steps

With `:steps` all β-reduction steps are shown one by one. `:auto` decides whether
it waits `:delay <ms>` between them or waits for you to press enter, and
`:settings` prints the current configuration.

```text
> :visu            toggle lambda diagrams
> :steps           toggle step-by-step reduction
> :auto            toggle automatic stepping
> :delay 300       set the delay between steps (ms)
> :settings        show the current configuration
```

In a real terminal the arrow up/down keys work like in a shell; the history is
kept in `~/.lambdaCalc_history`.

## Startup flags

Settings can also be passed on the command line (note the `--` separator, which
scala-cli requires before program arguments):

```bash
scala-cli run . -- --visu --steps --manual --delay 300
```

| Flag | Effect |
| --- | --- |
| `--visu`, `-v` | Start in lambda-diagram mode |
| `--steps` | Show every β-reduction step |
| `--auto` | Advance steps automatically |
| `--manual` | Wait for enter between steps |
| `--delay <ms>` | Delay between automatic steps |

## Project structure

```text
src/
├── main.scala                 REPL loop, argument parsing, command handling
├── settings.scala             immutable Settings record + defaults
├── console.scala              JLine-backed terminal input (history, editing)
├── calculation.scala          expression -> lambda term, numeral decoding
└── lambda/
    ├── lambda.scala           Church encodings (numerals, booleans, arithmetic)
    ├── betaReduction.scala    parser, beta-reduction, substitution, rendering
    └── johnTrompVisu.scala    ASCII John Tromp lambda diagrams

docs/                          documentation source (built with docmd)
docmd.config.json              documentation site configuration
.github/workflows/docs.yml     GitHub Pages build & deploy pipeline
```

## Building the documentation locally

The docs are built with [docmd](https://docmd.io) and deployed to GitHub Pages on
every push that touches `docs/` or `docmd.config.json`.

```bash
docmd dev      # live preview at http://localhost:3000
docmd build    # static site in ./site
docmd validate # check internal links
```
