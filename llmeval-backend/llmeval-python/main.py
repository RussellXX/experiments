# 主程序模块
# 该模块负责启动消费者进程，连接到 RabbitMQ 消息队列，接收请求并调用评估模块。

import pika
import json
from llmeval.openai import generate_text
from llmeval.evaluation import Evaluation

class Main:

    @staticmethod
    def start_consumer():
        """
        启动消费者进程，连接到 RabbitMQ 并监听请求队列
        """
        # 连接到 RabbitMQ 服务
        connection = pika.BlockingConnection(pika.ConnectionParameters('localhost'))
        channel = connection.channel()

        # 声明请求队列和结果队列
        channel.queue_declare(queue='promptEvalRequestQueue', durable=True)
        channel.queue_declare(queue='promptEvalResultQueue', durable=True)

        # 定义消费者函数
        def on_request(ch, method, properties, body):
            """
            处理请求队列中的消息，进行评估并返回结果
            """
            # 从请求队列中解析消息
            request = json.loads(body)
            eval_id = request["evalId"]
            metric_name = request["metric"]
            prompt = request["prompt"]
            ground_truth = request["groundTruth"]
            
            # 调用大模型生成响应
            response = generate_text(prompt)

            # 获取评估指标对象
            metric = Evaluation.create_metric(metric_name)
            
            # 调用 ragas 进行评估
            score = Evaluation.prompt_evaluate(metric, prompt, response, ground_truth)
            
            # 构建评估结果
            result = {
                "evalId": eval_id,
                "status": "DONE",
                "score": score,
                "generatedAnswer": response,
                "errorMsg": ""
            }

            # 将结果发送回结果队列
            channel.basic_publish(
                exchange='',
                routing_key='promptEvalResultQueue',
                body=json.dumps(result)
            )

        # 开始监听请求队列
        channel.basic_consume(queue='promptEvalRequestQueue', on_message_callback=on_request, auto_ack=True)


        print("main.py 66", flush=True) # test
        # 等待消息并处理
        channel.start_consuming()

        # print("main.py 68", flush=True) # test

# 启动消费者进程
if __name__ == "__main__":
    print("main 74", flush=True) # test
    Main.start_consumer()
