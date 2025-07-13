package com.wiredi.runtime;

import java.io.PrintStream;

public interface ConditionEvaluationReporter {

    PrintStreamReporter SYSTEM_OUT = new PrintStreamReporter(System.out);
    PrintStreamReporter SYSTEM_ERR = new PrintStreamReporter(System.err);

    void report(ConditionEvaluationContext context);

    class PrintStreamReporter implements ConditionEvaluationReporter {

        private final PrintStream printStream;

        public PrintStreamReporter(PrintStream printStream) {
            this.printStream = printStream;
        }

        @Override
        public void report(ConditionEvaluationContext context) {
            printStream.println();
            printStream.println("Condition Evaluation:");
            printStream.println("=====================");
            printStream.println("Providers         : " + context.providerCatalog().conditionalProviders().size());
            printStream.println("Applied           : " + context.appliedConditionalProviders().get());
            printStream.println("Condition Rounds  : " + context.additionalRounds().get());
            printStream.println("Round Threshold   : " + context.conditionalRoundThreshold());
            printStream.println();

            context.conditionEvaluation().forEach(evaluation -> {
                printStream.println("# " + evaluation.provider());
                if (!evaluation.positiveMatches().isEmpty()) {
                    printStream.println(" ++ Matched ++");
                    evaluation.positiveMatches().forEach(match -> printStream.println(" - " + match));
                }

                if (!evaluation.negativeMatches().isEmpty()) {
                    printStream.println(" -- Not Matched --");
                    evaluation.negativeMatches().forEach(match -> printStream.println(" - " + match));
                }
                printStream.println();
            });
            printStream.println("=====================");

        }
    }
}
