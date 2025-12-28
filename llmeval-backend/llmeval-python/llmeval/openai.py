# 大模型调用模块
# 该模块封装大模型的调用逻辑，使用提供的工具进行模型交互。

from ragas.llms import LangchainLLMWrapper
from ragas.embeddings import LangchainEmbeddingsWrapper
from langchain_openai import ChatOpenAI, OpenAIEmbeddings

class LangchainLLMWrapperFixTemperatureWrapper(LangchainLLMWrapper):
    """一个 LangchainLLMWrapper 的子类，用于修复温度设置问题"""

    def get_temperature(self, n: int) -> float:
        return 0.95

import os
API_KEY="12435978b26449fb9592d4e94532dc66.MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEAiHbWXSWakFbw96AYkECNFd0fVjw2JxbcWm4iM+ClQN4sGn0x4TzIG6wzY0Pc8wDFToQLbI1vAMuY/BfDM8QAwQIDAQABAkBvzmXdlVSo1ALD77CQZ8LwWeY14klAB5Psp3VwaasblHgevI+K6KlI3G+vvoZUlbATE2egL23As4Hv+dYw9FrRAiEA0D9Lr8fSaTaydPrxNmOTmsLd0hn2Sk8dpJUhV4KhQeUCIQCnwaxvu2L19w+Hh+EJZfZAqyqExKbgfyfAVQmHWG0FrQIhAMfyjixH0xpcfzpcm2+aamXlBnCtptGwZwjYe3v3m/eVAiAl4Z8dr/Pd1QdltHJFnAeqfLxiNMH2KeCBFvLK5FXiFQIgeTelKuDXThzykHe0ZNWHwCH9PtqQKsXgOqVTXZlCc0k="
os.environ["OPENAI_API_KEY"] = API_KEY

RAW_LLM = ChatOpenAI(
    temperature=0.95,
    model="glm-4-flash",
    openai_api_base="https://open.bigmodel.cn/api/paas/v4/",
    openai_api_key=API_KEY
)

RAW_EMBEDDING = OpenAIEmbeddings(
    model="embedding-3",
    openai_api_base="https://open.bigmodel.cn/api/paas/v4/",
    openai_api_key=API_KEY,
)

DEFAULT_LLM = LangchainLLMWrapperFixTemperatureWrapper(RAW_LLM)
DEFAULT_EMBEDDING = LangchainEmbeddingsWrapper(RAW_EMBEDDING)

def generate_text(prompt: str) -> str:
    """
    将提示词直接发送给大模型并返回生成结果
    
    Args:
        prompt: str
        
    Returns:
        str: 大模型生成的回复
    """

    response = RAW_LLM.invoke(prompt)
    return response.text()

    