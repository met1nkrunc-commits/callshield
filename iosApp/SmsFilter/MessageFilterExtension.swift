import IdentityLookup

final class MessageFilterExtension: ILMessageFilterExtension {}

extension MessageFilterExtension: ILMessageFilterQueryHandling {

    func handle(
        _ queryRequest: ILMessageFilterQueryRequest,
        context: ILMessageFilterExtensionContext,
        completion: @escaping (ILMessageFilterQueryResponse) -> Void
    ) {
        let response = ILMessageFilterQueryResponse()
        let decision = AppGroupStorage.shared.decision(
            sender: queryRequest.sender,
            body: queryRequest.messageBody
        )
        let action: ILMessageFilterAction = decision.shouldBlock ? .junk : .none
        response.action = action
        AppGroupStorage.shared.recordFilterInvocation(sender: queryRequest.sender, decision: decision)

        // Record filtered messages silently — no notification, user sees it in app.
        if action == .junk {
            AppGroupStorage.shared.recordFiltered(
                sender: queryRequest.sender ?? "",
                reason: decision.reason,
                detail: decision.detail,
                preview: queryRequest.messageBody ?? ""
            )
        }

        completion(response)
    }
}
