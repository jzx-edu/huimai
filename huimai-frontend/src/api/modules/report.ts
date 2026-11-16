import { request } from '../../utils/request'

export interface ReportParams {
  videoId?: number
  commentId?: number
  reason: string
  description?: string
}

/** 提交举报（视频或评论） */
export function submitReport(data: ReportParams) {
  return request({
    url: '/reports',
    method: 'post',
    data
  })
}
