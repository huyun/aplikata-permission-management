import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api', // 后端地址，根据实际情况修改
  timeout: 10000,
});

// 请求拦截器：自动添加 token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error),
);

// 响应拦截器：统一处理后端响应格式
api.interceptors.response.use(
  (response) => {
    // 假设后端返回 { code, message, data }
    const res = response.data;
    if (res.code !== 200) {
      const error = new Error(res.message || 'Request failed');
      error.code = res.code;
      return Promise.reject(error);
    }
    return res.data; // 直接返回 data 部分，方便使用
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response;
      const apiMessage = data?.message || 'Request failed';
      const apiData = data?.data; // 字段错误详情对象

      if (status === 401) {
        const message = apiMessage;
        if (!window.location.pathname.includes('/login')) {
          localStorage.removeItem('token');
          window.location.href = '/login';
        }
        return Promise.reject(new Error(message));
      } else if (status === 400) {
        // 参数验证错误：尝试从 apiData 中提取详细字段错误
        let detailedMessage = apiMessage;
        if (apiData && typeof apiData === 'object') {
          const fieldErrors = Object.entries(apiData)
            .map(([field, msg]) => `${field}: ${msg}`)
            .join('; ');
          if (fieldErrors) {
            detailedMessage = fieldErrors; // 例如 "password: Password must be at least 6 characters"
          }
        }
        return Promise.reject(new Error(detailedMessage));
      } else {
        return Promise.reject(new Error(apiMessage));
      }
    } else if (error.request) {
      return Promise.reject(new Error('Network error, please try again later'));
    } else {
      return Promise.reject(error);
    }
  },
);

export default api;
