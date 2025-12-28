# ragas 评估模块
# 该模块封装 ragas 库的评估工具，使用指定的评估指标对生成文本进行评分。

from ragas import SingleTurnSample
from ragas.metrics import Metric
from llmeval.openai import DEFAULT_LLM, DEFAULT_EMBEDDING
import time

class Evaluation:
    
    @staticmethod
    def create_metric(metric_name: str) -> Metric:
        """
        创建并返回对应的评估指标
        """
        metrics = {
            "BLEU_SCORE": "BleuScore",
            "ROUGE_SCORE": "RougeScore",
            "RESPONSE_RELEVANCY": "ResponseRelevancy",
            "SEMANTIC_SIMILARITY": "SemanticSimilarity",
            "FACTUAL_CORRECTNESS": "FactualCorrectness",
            "ANSWER_ACCURACY": "AnswerAccuracy"
        }
        metric_name = metrics[metric_name]
        module = __import__("ragas.metrics", fromlist=[metric_name])
        metric_class = getattr(module, metric_name)
        metric = metric_class(name=metric_name)
        metric.llm = DEFAULT_LLM
        metric.embeddings = DEFAULT_EMBEDDING
        return metric

    @staticmethod
    def prompt_evaluate(
        metric: Metric,
        prompt: str,
        response: str,
        ground_truth: str
    ) -> float:
        """
        使用 ragas 对生成的响应进行评估
        支持最大重试次数为 5
        """
        test_data = {
            'user_input': prompt,
            'response': response,
            'reference': ground_truth
        }

        test_data = SingleTurnSample(**test_data)

        attempts = 0
        max_retries = 5
        score = -1.0

        while attempts < max_retries:
            try:
                score = metric.single_turn_score(test_data)
                break  # 如果评估成功，跳出重试循环
            except Exception as e:
                attempts += 1
                if attempts >= max_retries:
                    raise Exception(f"Evaluation failed after {max_retries} attempts.")
                time.sleep(1)  # 等待一段时间后重试

        return score
