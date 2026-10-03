# LambdaCalc

lambdaCalc is a cli-calculator for transforming normal mathimatical terms into a lambda calculus and then solving them

# Usage
enter a equation like that: 6+14*5 , this will first do 14*5 then + 6
use $ to indicate that the folowing term should be interperted as a number: (this gives mostly unusable data out but it is possible to betareduct)
	$false^$true, false will be true times multipicated or
	$++$+, then plus will be added to plus
	$*+$false, then mulitplication will be sumed with false

# Lambda diagrams
start with --visu (or type :visu while running) to draw the terms as john Tromp lambda diagrams
instead of solving them, horizontal lines are the abstractions, vertical lines the variables and
the bars between them are applications

with :steps all the beta reduction steps are shown one by one, :auto decides if it waits
:delay <ms> between them or waits for you to press enter, and :settings prints the current config

in a real terminal the arrow up/down keys work like in a shell, the history is kept in ~/.lambdaCalc_history
