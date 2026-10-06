# Lambda Calculus Basics

The **lambda calculus** (λ-calculus) is a minimal formal system for expressing
computation. It was introduced by Alonzo Church in the 1930s and is one of the
foundations of functional programming and theoretical computer science.

LambdaCalc uses the lambda calculus as its computation model: mathematical
expressions are translated into lambda terms, and those terms are then evaluated
by the single rule of the calculus.

## The three constructions

A lambda term is built from only three things:

| Form | Name | Meaning |
| --- | --- | --- |
| $x$ | **variable** | a name, e.g. `x`, `f`, `m` |
| $\lambda x.\,M$ | **abstraction** | a function with parameter $x$ and body $M$ |
| $M\,N$ | **application** | apply the function $M$ to the argument $N$ |

That is all. There are no numbers, no booleans and no built-in operators - those
are *encoded* as functions (see [Church Encoding](/concepts/church-encoding)).

::: callout info "Formal grammar"
The whole language can be written as a three-line grammar:

$$
\begin{aligned}
M, N \;::=\;& x && \text{(variable)} \\
\mid\;& \lambda x.\,M && \text{(abstraction)} \\
\mid\;& M\,N && \text{(application)}
\end{aligned}
$$
:::

## Notation used by LambdaCalc

LambdaCalc writes abstractions with the Unicode λ and a dot:

```text
λx.λy.x
```

- Application associates to the **left**: $M\,N\,P$ means $(M\,N)\,P$.
- Abstraction extends as far to the **right** as possible: $\lambda x.\,M\,N$
  means $\lambda x.(M\,N)$.
- Parentheses are used to group, exactly as in ordinary arithmetic.

## Two tiny examples

The **identity function**, which simply returns its argument:

$$
\lambda x.\,x
$$

The function that takes two arguments and returns the first - the Church `true`:

$$
\lambda x.\lambda y.\,x
$$

## Why it matters

Every computable function can be expressed in the lambda calculus - it is
[Turing-complete](https://en.wikipedia.org/wiki/Lambda_calculus). Because the
whole language is three constructions and one reduction rule, it is an excellent
model to reason about computation, and a surprisingly good way to build a
calculator that *can theoretically do anything*.

::: callout tip "Further reading"
The [Wikipedia article on the lambda calculus](https://en.wikipedia.org/wiki/Lambda_calculus)
and the [CS704 lambda calculus notes](https://pages.cs.wisc.edu/~horwitz/CS704-NOTES/1.LAMBDA-CALCULUS.html)
are good starting points. See also [Sources](/sources).
:::
