# Command Line & Commands

LambdaCalc has a small but useful set of startup flags and interactive commands.

## Starting the calculator

```bash
scala-cli run .
```

Because scala-cli needs `--` before arguments meant for the program, pass flags
like this:

```bash
scala-cli run . -- --visu --steps --manual --delay 300
```

## Startup flags

Parsed by `main.parseArgs` in `src/main.scala`:

| Flag | Short | Effect |
| --- | --- | --- |
| `--visu` | `-v` | Start in lambda-diagram mode |
| `--steps` | | Show every β-reduction step |
| `--auto` | | In step mode, advance automatically |
| `--manual` | | In step mode, wait for enter between steps |
| `--delay <ms>` | | Milliseconds between automatic steps |

::: callout info "Unknown flags"
Unknown flags are silently ignored: `parseArgs` falls through to the rest of the
list rather than erroring.
:::

## Interactive commands

While running, any line beginning with `:` is treated as a command rather than an
equation. Commands toggle settings and print their new state.

::: tabs

== Command reference

| Command | Description |
| --- | --- |
| `:visu` | Toggle lambda-diagram mode instead of solving |
| `:steps` | Toggle showing every β-reduction step |
| `:auto` | Toggle automatic stepping (no key press needed) |
| `:delay <ms>` | Set the delay between automatic steps (≥ 0) |
| `:settings` | Print the current configuration |

== Example session

```text
> :visu
lambda diagrams: on
> :steps
beta reduction steps: on
> :auto
auto steps: on
> :delay 300
step delay: 300 ms
> :settings
settings:
  :visu          lambda diagrams instead of solving   on
  :steps         show every beta reduction step       on
  :auto          step by itself, no key needed        on
  :delay <ms>    wait between steps in auto mode      300 ms
  :settings      show this
```

:::

::: callout warning "Unknown commands"
Entering a command LambdaCalc does not recognise prints
`unknown command, try :settings`.
:::

## Quitting

Press enter on an empty line (or send EOF / Ctrl-C) to quit - the calculator
prints `bye` and exits.

## History

In a real terminal the arrow keys work like in a shell: JLine provides command
editing and history. The history is stored in `~/.lambdaCalc_history` (created
with `rw-------` permissions). When input is piped (no TTY), LambdaCalc falls back
to plain line reading without history.

::: callout tip "Piped input works"
Because of the fallback, you can also pipe expressions in without a terminal:

```bash
echo "2^8" | scala-cli run . --quiet
```
:::
