# Visualisation & Reduction Steps

Two features let you look *inside* a computation instead of only seeing its
answer: **lambda diagrams** and **reduction steps**.

## Lambda diagrams (`:visu`)

Toggle with `--visu` at startup or `:visu` while running. In this mode LambdaCalc
does not print the answer; it prints the diagram of the input term and of its
normal form:

```text
> :visu
lambda diagrams: on
> 2
lambda term
<diagram of the encoded term>
normal form
<diagram of the normal form>
```

See [Lambda Diagrams](/concepts/lambda-diagrams) for the drawing rules.

## Reduction steps (`:steps`)

Toggle with `--steps` or `:steps`. Every term from the input down to the
β-normal form is printed one after another, labelled with its index:

```text
> :steps
beta reduction steps: on
> 1+1
step 0 / 4
  (λm.λn.λf.λx. m f (n f x))(λf.λx.f x)(λf.λx.f x)
step 1 / 4
  ...
step 4 / 4 (normal form)
  λf.λx.f(f x)
```

If diagrams are also on (`:visu` **and** `:steps`), each step is drawn as a
diagram instead of printed as text.

::: callout tip "Combine the two"
`:steps` and `:visu` are independent; enabling both gives you a step-by-step
animated diagram of the reduction.
:::

## Pacing the steps

Between steps LambdaCalc either waits automatically or for a key press:

| Mode | Command | Behaviour |
| --- | --- | --- |
| Auto | `:auto` | Waits `stepDelay` milliseconds, then continues |
| Manual | `:manual` | Prints `(enter to continue)` and waits for enter |

Set the delay with:

```text
> :delay 300
step delay: 300 ms
```

The delay is in milliseconds and must be `≥ 0`; a negative value is rejected with
`delay must be >= 0`.

## Defaults

| Setting | Default |
| --- | --- |
| `:visu` | off |
| `:steps` | off |
| `:auto` | on |
| `:delay` | 700 ms |

Use `:settings` at any time to print the current values.
